package uk.co.jcox.chemvis.application.moleditor

import org.joml.Vector2f
import org.joml.Vector3f
import org.lwjgl.system.linux.XSelectionEvent
import uk.co.jcox.chemvis.application.MolGLide
import uk.co.jcox.chemvis.application.moleditor.actions.AtomCreationAction
import uk.co.jcox.chemvis.application.moleditor.actions.AtomInsertionAction
import uk.co.jcox.chemvis.cvengine.scenegraph.EntityLevel
import uk.co.jcox.chemvis.cvengine.scenegraph.ObjComponent
import uk.co.jcox.chemvis.cvengine.scenegraph.TransformComponent
import java.util.UUID


/**
 *This tool can dispatch two actions: AtomCreation and AtomInsertion
 */
class AtomBondTool(context: ToolCreationContext) : Tool(context) {

    private var draggingAtom: UUID? = null
    private var dragBase = Vector3f()

    override fun update() {

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

}