package uk.co.jcox.chemvis.application.moleditor

import io.github.dan2097.jnainchi.inchi.InchiLibrary
import org.checkerframework.checker.units.qual.mol
import org.joml.Vector2f
import org.joml.Vector3f
import org.xmlcml.euclid.Vector3
import uk.co.jcox.chemvis.application.MolGLide
import uk.co.jcox.chemvis.application.moleditor.actions.AtomCreationAction
import uk.co.jcox.chemvis.application.moleditor.actions.AtomInsertionAction
import uk.co.jcox.chemvis.application.moleditor.actions.BondOrderAction
import uk.co.jcox.chemvis.cvengine.scenegraph.EntityLevel
import uk.co.jcox.chemvis.cvengine.scenegraph.ObjComponent
import uk.co.jcox.chemvis.cvengine.scenegraph.TextComponent
import uk.co.jcox.chemvis.cvengine.scenegraph.TransformComponent
import java.util.UUID


/**
 *This tool can dispatch two actions: AtomCreation and AtomInsertion
 */
class AtomBondTool(context: ToolCreationContext) : Tool(context) {

    private var draggingAtom: UUID? = null
    private var dragBase: Vector3f = Vector3f()
    private var allowExternalBondFormation = true
    private var allowStateRestore = true

    private var potentialDraggingPosition: Vector2f = Vector2f()

    private var wasInsideRegion = false

    override fun update() {

        updateDraggingPosition()

        val atomDragID = draggingAtom
        val selection = context.selectionManager.primarySelection

        if (atomDragID == null || selection !is Selection.Active) {
            return
        }

        val atomSelect = workingState.level.findByID(selection.id)

        if (atomSelect != null) {
            checkForBondOrderCancellations(atomSelect)
        }

        val atomDrag = workingState.level.findByID(atomDragID)

        if (atomDrag == null || atomSelect == null) {
            return
        }

        updateGhostHydrogenGroups(atomDrag, atomSelect)

        checkForBondOrderChanges(atomDrag, atomSelect)

    }

    override fun processClick(clickDetails: ClickContext) {

        val primarySelection = context.selectionManager.primarySelection

        if (primarySelection is Selection.None) {
            //Just make a new molecule with the atom selected
            insertNewMolecule(clickDetails)
        }

        if (primarySelection is Selection.Active) {
            //In this case, as an atom is selected in the editor we want to add a new atom, but also make a new bond with the selected atom
            //This is still (kind of) one-step process so no intermediate restore point is required
            //However, a live preview is required so actionInProgress is set to true
            val selectedID = primarySelection.id
            val selectedLevel = workingState.level.findByID(selectedID)

            if (selectedLevel == null) {
                return
            }

            val moleculeLevel = LevelViewUtil.getLvlMolFromLvlAtom(selectedLevel)

            if (moleculeLevel == null) {
                return
            }

            addAtomToMolecule(clickDetails, moleculeLevel, selectedLevel)
        }
    }

    override fun processClickRelease(clickDetails: ClickContext) {
        if (actionInProgress) {
            draggingAtom = null
            allowExternalBondFormation = true
            allowStateRestore = true
            pushChanges()
        }
    }


    override fun renderTransientUI(transientUI: EntityLevel) {
        renderSelectionMarkerOnAtoms(transientUI)
    }


    private fun checkForBondOrderCancellations(atomSelect: EntityLevel) {
        //At this stage, the dragging atom no longer exists because the state was refreshed
        //So given the mouse position and the atom selected we can figure out if we need to cancel the bond order change

        if (actionInProgress && !allowExternalBondFormation) {
            val mousePos = mouseWorld()

            val matcher = findMatchingPosition(workingState.level, Vector3f(potentialDraggingPosition, NewOrganicEditorState.XY_PLANE), null)

            val isInsideRegion = matcher != null

            allowStateRestore = !(wasInsideRegion && isInsideRegion)

            if (matcher == null && allowStateRestore) {
                //User no wants to form a double bond
                //So just restore the old state
                restoreOnce()
                updateDraggingPosition()
                allowExternalBondFormation = true
                allowStateRestore = false
            }
        }


    }

    private fun checkForBondOrderChanges(atomDrag: EntityLevel, atomSelect: EntityLevel) {
        //To check if a double bond is needed, or a cyclic structure is needed
        //the dragging atom's position is checked to see if hits another atom
        //if it does, a bond is formed (cylic formation) or a bond order change takes place (double bond formation)

        val draggingPos = atomDrag.getAbsolutePosition()
        val matcher = findMatchingPosition(workingState.level, draggingPos, atomDrag)

        val parent = LevelViewUtil.getLvlMolFromLvlAtom(atomDrag)

        if (parent == null) {
            return
        }

        matcher?.let {


            if (!allowExternalBondFormation && !actionInProgress) {
                return
            }

            makeRestorePoint()

            //Reverting the AtomInsertionEvent from being commited
            refreshWorkingState(false)

            //This is a multi-step process
            //A user might make a double bond, but then move the dragging atom away
            //Before making the new bond, we first tell the Tool to save state
            //Then we can make changes by calling the action on this state
            //If the user drags away, we need to restore the old state
            actionInProgress = true

            val atomA = workingState.level.findByID(it.id)
            val atomB = workingState.level.findByID(atomSelect.id)
            val mol = workingState.level.findByID(parent.id)

            //Make sure the new entities are not null
            if (atomA == null || atomB == null || mol == null) {
                return
            }

            val action = BondOrderAction(mol, atomA, atomB)
            action.runAction(workingState.molManager,  workingState.level)
            allowExternalBondFormation = false
        }
    }



    private fun findMatchingPosition(lvl: EntityLevel, posToCheck: Vector3f, discard: EntityLevel?) : EntityLevel? {
        var matcher: EntityLevel? = null
        lvl.traverseFunc {
            //Check for match, but make sure to not select itself
            if (it.getAbsolutePosition() == posToCheck && it != discard) {
                matcher = it
                return@traverseFunc
            }
        }
        return matcher
    }

    private fun updateDraggingPosition() {


        val mouseWorld = mouseWorld()
        potentialDraggingPosition = closestPointToCircleCircumference(Vector2f(dragBase.x, dragBase.y), mouseWorld, NewOrganicEditorState.CONNECTION_DIST)

        val atomToDrag = draggingAtom

        if (! actionInProgress || atomToDrag == null) {
            return
        }

        //Make the atom of interest follow the mouse
        val draggingAtomlevel = workingState.level.findByID(atomToDrag)

        if (draggingAtomlevel == null) {
            return
        }

        val parent = LevelViewUtil.getLvlMolFromLvlAtom(draggingAtomlevel)

        if (parent == null) {
            return
        }

        val effectiveParentPos = parent.getAbsolutePosition()



        val entityTransform = draggingAtomlevel.getComponent(TransformComponent::class)

        entityTransform.x = potentialDraggingPosition.x - effectiveParentPos.x
        entityTransform.y = potentialDraggingPosition.y - effectiveParentPos.y
    }


    private fun updateGhostHydrogenGroups(atomDrag: EntityLevel, atomSelect: EntityLevel) {
        //If when placing a bond, and you place it through an implicit hydrogen group
        //Then that hydrogen group should move to a different side of the bond

        val draggingTransform = atomDrag.getComponent(TransformComponent::class)
        moveGhostGroup(Vector2f(draggingTransform.x, draggingTransform.y), atomSelect)

        //todo still not working for the dragging atom?
        val selectingTransform = atomSelect.getComponent(TransformComponent::class)
//        moveGhostGroup(Vector2f(selectingTransform.x, selectingTransform.y), atomDrag)
    }

    private fun moveGhostGroup(oppositeEnd: Vector2f, atom: EntityLevel) {

        //1) Find the ghost groups and get their position
        var ghostGroup: EntityLevel? = null
        atom.traverseFunc {
            if (it.hasComponent(GhostImplicitHydrogenGroupComponent::class)) {
                ghostGroup = it
                return@traverseFunc
            }
        }

        val groupPos = ghostGroup?.getComponent(TransformComponent::class)
        val atomPos = atom.getComponent(TransformComponent::class)

        if (groupPos == null) {
            return
        }

        val currentPos = Vector3f(groupPos.x + atomPos.x, groupPos.y + atomPos.y, groupPos.z + atomPos.z)

        //2) Find the lengths of the positions in respect to where the bond is
        val test1 = oppositeEnd.distance(currentPos.x, currentPos.y)
        val test2 = oppositeEnd.distance(-currentPos.x, -currentPos.y)

        //3) If they are equal, then just keep everything how it is
        if (test1 == test2) {
            return
        }

        if (test1 > test2) {
            //if the initial test is closer, leave as is
            return
        }

        //4) Otherwise move the group to the other potential site
        //However, when moving the group, if the group is to the left of the atom, the text space needs to be considered
        val newPos = getPositionForGhostPlacement(ghostGroup)
        val transformComp = ghostGroup.getComponent(TransformComponent::class)
        transformComp.x = newPos.x
        transformComp.y = newPos.y
        transformComp.z = newPos.z
    }

    private fun getPositionForGhostPlacement(ghostGroup: EntityLevel) : Vector3f {
        if (!ghostGroup.hasComponent(TextComponent::class)) {
            return Vector3f(0.0f, 0.0f, 0.0f)
        }

        val textComp = ghostGroup.getComponent(TextComponent::class)
        val lengthOffset = textComp.text.length * NewOrganicEditorState.INLINE_DIST /2f
        val ghostGroupPos = ghostGroup.getComponent(TransformComponent::class)
        //Because the text itself consumes space, the position of the group needs to be adjusted
        return Vector3f(-lengthOffset - ghostGroupPos.x, ghostGroupPos.y, NewOrganicEditorState.XY_PLANE)
    }

    private fun insertNewMolecule(clickDetails: ClickContext) {
        //NOTE - This is a very simple action that does not involve any intermediate steps
        //Therefore no checkpoint/restore point is needed
        //So just run the action as and then wait for push changes to trigger
        actionInProgress = true
        val action = AtomCreationAction(clickDetails.xPos, clickDetails.yPos, clickDetails.atomSelected)
        action.runAction(workingState.molManager, workingState.level)
    }


    private fun addAtomToMolecule(clickDetails: ClickContext, moleculeLevel: EntityLevel, selectedLevel: EntityLevel) {

        //Everything is ready to start now
        actionInProgress = true

        val action = AtomInsertionAction(clickDetails.xPos, clickDetails.yPos, clickDetails.atomSelected, moleculeLevel, selectedLevel)
        action.runAction(workingState.molManager, workingState.level)
        //This action also returns an ID for the newly created atom, this allows us to "drag" the atom around
        draggingAtom = action.insertedAtom
        dragBase =  selectedLevel.getAbsolutePosition()
    }
}