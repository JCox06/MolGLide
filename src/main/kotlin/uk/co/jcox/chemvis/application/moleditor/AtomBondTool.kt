package uk.co.jcox.chemvis.application.moleditor

import org.checkerframework.checker.units.qual.m
import org.joml.Vector2f
import org.joml.Vector3f
import org.openscience.cdk.smiles.smarts.parser.SMARTSParserConstants.a
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

        //Handle when the user is dragging an atom
        val atomToDrag = draggingAtom

        if (! actionInProgress || atomToDrag == null) {
            return
        }

        //Make the atom of interest follow the mouse
        val draggingAtomLevel = getWorkingState().level.findByID(atomToDrag)
        val parentEntity = draggingAtomLevel?.parent

        if (draggingAtomLevel == null || parentEntity == null) {
            return
        }

        val effectiveParentPos = parentEntity.getAbsolutePosition()

        val mouseWorld = context.camera2D.screenToWorld(context.inputManager.mousePos())
        val draggedPos = closestPointToCircleCircumference(Vector2f(dragBase.x, dragBase.y), mouseWorld, NewOrganicEditorState.CONNECTION_DIST)

        val entityTransform = draggingAtomLevel.getComponent(TransformComponent::class)
        entityTransform.x = draggedPos.x - effectiveParentPos.x
        entityTransform.y = draggedPos.y - effectiveParentPos.y

        //Now we need to move the implicit Ghost hydrogen group if the new bond position is going through it


        //For the pre-existing atom
        if (context.selectionManager.primarySelection is Selection.Active) {
            val select = context.selectionManager.primarySelection as Selection.Active
            val selecAtom = getWorkingState().level.findByID(select.id)
            selecAtom?.let {
                moveImplicitHydrogenGhostGroup(Vector2f(entityTransform.x, entityTransform.y), it)

                //For the atom being dragged
                val selecAtomTrans = selecAtom.getComponent(TransformComponent::class)
                //todo - This is not working properly, so commenting out for now and coming back to it later
//                moveImplicitHydrogenGhostGroup(Vector2f(selecAtomTrans.x, selecAtomTrans.y), draggingAtomLevel)




                //TODO - JUST PUTTING THIS HERE FOR NOW!
                //We also need to check if the user drags the dragging atom to the position of the selected atom
                //If they do this, then we need to ignore the previous action, and create a DoubleBondInsertionAction
                checkForBondOrderChange(parentEntity, selecAtom)
            }
        }
    }

    override fun updateProposedModifications() {

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


    private fun checkForBondOrderChange(levelMolecule: EntityLevel, selecAtom: EntityLevel) {
        //Get position of the dragging atom First
        val atomDrag = draggingAtom
        if (atomDrag == null) {
            return
        }

        val atomDragEntity = getWorkingState().level.findByID(atomDrag)

        if (atomDragEntity == null) {
            return
        }

        val draggingAtomPos = atomDragEntity.getAbsolutePosition()

        //Now see if it matches any of the atoms in the scenegraph

        var matcher: EntityLevel? = null
        getWorkingState().level.traverseFunc {
            if (it.getAbsolutePosition() == draggingAtomPos && it != atomDragEntity) {
                matcher = it
                return@traverseFunc
            }
        }

        matcher?.let {
            //Now send a bond order action
            if (allowBondOrderChanges) {
                refreshWorkingState()
                actionInProgress = true
                //Stop the new atom or whatever being added and restore the working state to that of the actual level


                val action = BondOrderAction(levelMolecule, it, selecAtom)
                action.runAction(getWorkingState().molManager, getWorkingState().level)
                allowBondOrderChanges = false
            }
        }
    }
}