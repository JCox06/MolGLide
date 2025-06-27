package uk.co.jcox.chemvis.application.moleditor

import org.joml.Vector2f
import org.joml.Vector3f
import org.joml.minus
import uk.co.jcox.chemvis.application.moleditor.actions.AtomCreationAction
import uk.co.jcox.chemvis.cvengine.scenegraph.EntityLevel
import uk.co.jcox.chemvis.cvengine.scenegraph.TransformComponent

class NewAtomBondTool(context: ToolCreationContext) : Tool(context){

    private var toolMode: Mode = Mode.None

    override fun update() {

        checkSwitchToInsertionMode()
        updateDraggingPosition()
    }


    override fun processClick(clickDetails: ClickContext) {

        toolMode = selectMode(clickDetails)

        //Make a restore point every click
        //Actions that are fired below in the "when" block are done on click
        //Some actions however want to be deferred. For example, an insertion only happens if the mode is first in replace,
        //but will switch to insertion upon the mouse moving
        makeRestorePoint()

        when (val mode = toolMode) {
            is Mode.None -> {}

            is Mode.Dragging -> {}

            is Mode.Placement -> addMolecule(mode)

            is Mode.Replacement -> replaceElement(mode)
        }
    }

    override fun processClickRelease(clickDetails: ClickContext) {

        //If the mode is something other than none, then commit the local stack to the main stack
        if (toolMode !is Mode.None) {
            pushChanges()
        }

        resetState()
    }

    private fun resetState() {
        toolMode = Mode.None
        refreshWorkingState(true)
    }

    override fun renderTransientUI(transientUI: EntityLevel) {
        renderSelectionMarkerOnAtoms(transientUI)
    }


    private fun selectMode(clickDetails: ClickContext): Mode {
        val selectionMode = context.selectionManager.primarySelection

        when (val selector = selectionMode) {
            is Selection.None -> return Mode.Placement(clickDetails.xPos, clickDetails.yPos, clickDetails.insert)

            is Selection.Active -> {
                val selectedID = selector.id
                val selectedEntity = workingState.level.findByID(selectedID)
                if (selectedEntity == null) {
                    return Mode.None
                }
                return Mode.Replacement(selectedEntity, clickDetails.insert)
            }
        }
    }

    //This method will determine if we should switch to insertion mode every update
    //If we should, then it will handle the switch and call the appropriate action
    private fun checkSwitchToInsertionMode() {
        //First check if we are in the correct state
        //to enter insertion mode, you need to click and drag, so you need to have first clicked (be in replacement mode)
        //To test for dragging, check the deltaMousePos
        val mode = toolMode
        if (mode !is Mode.Replacement) {
            //Do not change mode
            return
        }

        val newInsert = mode.replacement
        val selectedAtom = mode.atom

        //Check to see if the user has moved a substantial distance
        val dMouse = context.inputManager.deltaMousePos()
        val mouse = mouseWorld()
        if (dMouse.length() >= SIG_DELTA) {
            //Switch modes! - First restore, call the new action, and change the state of bond mode
            restoreOnce()
            //After restoring collect new references
            val newSelectedAtom = workingState.level.findByID(selectedAtom.id)

            if (newSelectedAtom == null) {
                resetState()
                return
            }

            val insertedAtom = insertAtom(mouse.x, mouse.y, newInsert, selectedAtom)

            if (insertedAtom == null) {
                resetState()
                return
            }

            toolMode = Mode.Dragging(insertedAtom, newSelectedAtom, Vector2f(mouse.x, mouse.y))
        }
    }


    private fun updateDraggingPosition() {
        //Check the mode first
        val tool = toolMode

        if (tool !is Mode.Dragging) {
            return
        }

        //Then get the new position
        val mouse = mouseWorld()
        val stationaryPos = tool.stationaryAtom.getAbsolutePosition()
        val newPos = closestPointToCircleCircumference(Vector2f(stationaryPos.x, stationaryPos.y), mouse, NewOrganicEditorState.CONNECTION_DIST)

        tool.proposedDragPos = newPos

        //Apply the proposed position
        val draggingTrans = tool.draggingAtom.getAbsoluteTranslation()
        val localTrans = Vector3f(newPos, NewOrganicEditorState.XY_PLANE) - draggingTrans

        val transformComp = tool.draggingAtom.getComponent(TransformComponent::class)
        transformComp.x = localTrans.x
        transformComp.y = localTrans.y
        transformComp.z = localTrans.z
    }


    //Each mode is associated with an action
    //The following methods are responsible for creation the appropriate actions

    private fun addMolecule(placement: Mode.Placement) {
        val action = AtomCreationAction(placement.xPos, placement.yPos, placement.insert)
        action.runAction(workingState.molManager, workingState.level)
    }

    private fun replaceElement(replacement: Mode.Replacement) {
//        val action = ElementalEditAction(replacement.atom, replacement.replacement)
//        action.runAction(workingState.molManager, workingState.level)
    }


    private fun insertAtom(initXPos: Float, initYPos: Float, insert: AtomInsert, levelAtom: EntityLevel) : EntityLevel? {

//        val levelMoleculeID = LevelViewUtil.getLvlMolFromLvlAtom(levelAtom)
//
//        if (levelMoleculeID == null) {
//            return null
//        }
//
//        val levelMolecule = workingState.level.findByID(levelMoleculeID)
//
//        if (levelMolecule == null) {
//            return null
//        }
//
//        val action = AtomInsertionAction(initXPos, initYPos, insert, levelMolecule, levelAtom)
//        action.runAction(workingState.molManager, workingState.level)
//
//        val toReturn = action.insertedAtom
//
//        return toReturn

        return null
    }


    override fun inProgress(): Boolean {
        return toolMode !is Mode.None
    }

    sealed class Mode {
        object None: Mode()
        data class Placement(val xPos: Float, val yPos: Float, val insert: AtomInsert) : Mode()
        data class Replacement(val atom: EntityLevel, val replacement: AtomInsert) : Mode()
        data class Dragging(val draggingAtom: EntityLevel, val stationaryAtom: EntityLevel, var proposedDragPos: Vector2f) : Mode()
    }

    companion object {
        private const val SIG_DELTA = 7.5f
    }
}
