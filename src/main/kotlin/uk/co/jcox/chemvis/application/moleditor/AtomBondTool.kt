package uk.co.jcox.chemvis.application.moleditor

import io.github.dan2097.jnainchi.inchi.InchiLibrary
import org.joml.Vector2f
import org.joml.Vector3f
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
    private var dragBase = Vector3f()

    private var allowBondOrderChanges = true

    override fun update() {

        val atomDragID = draggingAtom
        val selection = context.selectionManager.primarySelection

        if (atomDragID != null && actionInProgress && selection is Selection.Active) {

            val atomDrag = getWorkingState().level.findByID(atomDragID)
            val selectedID = selection.id
            val atomSelect = getWorkingState().level.findByID(selectedID)
            if (atomDrag != null && atomSelect != null) {
                updateDraggingAtomPosition(atomDrag)
                moveGhostHydrogenGroups(atomDrag)
                checkForBondOrderChange(atomDrag, atomSelect)
            }

        }
    }


    private fun updateDraggingAtomPosition(draggingAtomLevel: EntityLevel) {

        val parentEntity = draggingAtomLevel.parent

        if (parentEntity == null) {
            return
        }

        val effectiveParentPos = parentEntity.getAbsolutePosition()

        val mouseWorld = context.camera2D.screenToWorld(context.inputManager.mousePos())
        val draggedPos = closestPointToCircleCircumference(Vector2f(dragBase.x, dragBase.y), mouseWorld, NewOrganicEditorState.CONNECTION_DIST)

        val entityTransform = draggingAtomLevel.getComponent(TransformComponent::class)
        entityTransform.x = draggedPos.x - effectiveParentPos.x
        entityTransform.y = draggedPos.y - effectiveParentPos.y
    }

    private fun moveGhostHydrogenGroups(draggingAtomLevel: EntityLevel) {

        if (context.selectionManager.primarySelection !is Selection.Active || draggingAtomLevel == null) {
            return
        }
        val draggingTransform = draggingAtomLevel.getComponent(TransformComponent::class)

        val select = context.selectionManager.primarySelection as Selection.Active
        val selectedAtom = getWorkingState().level.findByID(select.id)
        selectedAtom?.let {
            moveImplicitHydrogenGhostGroup(Vector2f(draggingTransform.x, draggingTransform.y), it)
        }

        //todo - FIX BELOW
//          moveImplicitHydrogenGhostGroup(Vector2f(selecAtomTrans.x, selecAtomTrans.y), draggingAtomLevel)
    }


    private fun checkForBondOrderChange(atomDrag: EntityLevel, selecAtom: EntityLevel) {

        //Check if the dragging pos matches any other pos in the scenegraph
        val draggingPos = atomDrag.getAbsolutePosition()

        val matcher = findMatchingPosition(getWorkingState().level, draggingPos, atomDrag)

        val parent = atomDrag.parent

        if (parent == null) {
            return
        }

        matcher?.let {

            //Because this is run every frame, this should only be fired once
            if (! allowBondOrderChanges) {
                return
            }
            refreshWorkingState(false)
            actionInProgress = true

            //Refreshing the state stops the new atom being added
            //However in doing so all the references to the entities are now of that from the old state
            //Therefore new references are required from the new working state
            val atomA = getWorkingState().level.findByID(it.id)
            val atomB = getWorkingState().level.findByID(selecAtom.id)
            val mol = getWorkingState().level.findByID(parent.id)

            //Make sure the new entities are not null
            if (atomA == null || atomB == null || mol == null) {
                return
            }

            val action = BondOrderAction(mol, atomA, atomB)
            action.runAction(getWorkingState().molManager,  getWorkingState().level)
            allowBondOrderChanges = false
        }
    }



    private fun findMatchingPosition(lvl: EntityLevel, posToCheck: Vector3f, discard: EntityLevel) : EntityLevel? {
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


    override fun processClick(clickDetails: ClickContext) {
        //First check if something is selected
        if (context.selectionManager.primarySelection is Selection.None) {
            //In this case, the action is quite simple
            val action = AtomCreationAction(clickDetails.xPos, clickDetails.yPos, clickDetails.atomSelected)
            action.runAction(getWorkingState().molManager, getWorkingState().level)
            pushChanges()
        }

        val atomIDGetter = context.selectionManager.primarySelection

        if (atomIDGetter is Selection.Active) {

            //Get the atom associated with the click
            val atomID = atomIDGetter.id
            val atomLevel = getWorkingState().level.findByID(atomID)
            val moleculeLevel = atomLevel?.parent

            if (atomLevel != null && moleculeLevel != null) {

                //In this case we want to add a new bond and atom to an existing element
                //This requires the transient UI to temp edit the current state
                actionInProgress = true

                val action = AtomInsertionAction(clickDetails.xPos, clickDetails.yPos, clickDetails.atomSelected, moleculeLevel, atomLevel, true)
                action.runAction(getWorkingState().molManager, getWorkingState().level)
                draggingAtom = action.insertedAtom
                dragBase = atomLevel.getAbsolutePosition()
            }
        }
    }

    override fun processClickRelease(clickDetails: ClickContext) {

        if (actionInProgress) {
            draggingAtom = null
            allowBondOrderChanges = true
            pushChanges()
        }
    }

    override fun renderTransientUI(transientUI: EntityLevel) {
        val currentSelection = context.selectionManager.primarySelection
        if (currentSelection !is Selection.Active) {
            return
        }

        val entitySelected = getWorkingState().level.findByID(currentSelection.id)
        val position = entitySelected?.getAbsolutePosition()
        if (position == null) {
            return
        }

        val selectionMarker = transientUI.addEntity()
        selectionMarker.addComponent(TransformComponent(position.x, position.y, position.z, NewOrganicEditorState.SELECTION_MARKER_SIZE))
        selectionMarker.addComponent(ObjComponent(MolGLide.SELECTION_MARKER_MESH, MolGLide.SELECTION_MARKER_MATERIAL))
    }


    private fun moveImplicitHydrogenGhostGroup(bondLineEnd: Vector2f, atom: EntityLevel) {
        //Moves the implicit hydrogen ghost group if the bond is going through it
        //The action event will handle removing it


        //1) Get the implicit hydrogen group
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

        val newAtomPos = Vector3f(groupPos.x + atomPos.x, groupPos.y + atomPos.y, groupPos.z + atomPos.z)


        //2) Check if the bond line end is close to the group position
        val initialTest = bondLineEnd.distance(newAtomPos.x, newAtomPos.y)

        //3) Now check the distance if we use the alternative position
        val secondTest = bondLineEnd.distance(-newAtomPos.x, -newAtomPos.y)

        if (initialTest == secondTest) {
            return
        }

        if (initialTest > secondTest) {
            //4) If the initial test is closer, we can just leave it as is
            return
        }


        //5) If the second test is closer, we need to move the group
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

}