package uk.co.jcox.chemvis.application.moleditor

import org.joml.Vector2f
import org.joml.Vector3f
import org.joml.minus
import org.xmlcml.euclid.Vector2
import uk.co.jcox.chemvis.application.moleditor.actions.AtomCreationAction
import uk.co.jcox.chemvis.application.moleditor.actions.AtomInsertionAction
import uk.co.jcox.chemvis.application.moleditor.actions.BondOrderAction
import uk.co.jcox.chemvis.application.moleditor.actions.ElementalEditAction
import uk.co.jcox.chemvis.cvengine.scenegraph.EntityLevel
import uk.co.jcox.chemvis.cvengine.scenegraph.TransformComponent

class NewAtomBondTool(context: ToolCreationContext) : Tool(context){

    private var toolMode: Mode = Mode.None

    override fun update() {
        checkSwitchToInsertionMode()
        updateDraggingPosition()
        checkSwitchToBondJoin()
        checkRevertBondJoin()
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

            val insertedAtom = insertAtom(newInsert, newSelectedAtom)

            if (insertedAtom == null) {
                resetState()
                return
            }

            toolMode = Mode.Dragging(insertedAtom, newSelectedAtom, Vector2f(mouse.x, mouse.y), true, false)
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


    private fun checkSwitchToBondJoin() {
        val tool = toolMode

        if (tool !is Mode.Dragging || !tool.allowBondOrderChange) {
            return
        }

        val matcher = findMatchingPosition(workingState.level, tool.proposedDragPos, tool.draggingAtom)

        matcher?.let {
            //Allow you to cancel this action if you drag away from the position
            makeRestorePoint()

            //Stop the atom replacement AND atom insertion from being commited
            refreshWorkingState(false)

            val atomA = workingState.level.findByID(it.id)
            val atomB = workingState.level.findByID(tool.stationaryAtom.id)


            if (atomA == null || atomB == null) {
                return
            }

            tool.allowBondOrderChange = false

            insertBondOrderChange(atomA, atomB)
        }
    }

    private fun checkRevertBondJoin() {
        //if the user pulls away after a successful switch to BondJoin, then we need to revert everything
        val tool = toolMode

        if (tool !is Mode.Dragging || tool.allowBondOrderChange) {
            return
        }

        val matcher = findMatchingPosition(workingState.level, tool.proposedDragPos, null)


        if (matcher == null) {
            restoreOnce()
            updateDraggingPosition()
            tool.allowBondOrderChange = true
            tool.allowStateRestore = true

            val newDragging = workingState.level.findByID(tool.draggingAtom.id)
            val newStationary = workingState.level.findByID(tool.stationaryAtom.id)

            //Restoring invalidates all current references
            //todo temp measure - Really should check these are available before switching to the previous state
            if (newDragging == null || newStationary == null) {
                throw Exception("TEMP ERROR")
            }
            toolMode = Mode.Dragging(newDragging, newStationary, Vector2f(), true, true)
        }

    }

    private fun findMatchingPosition(lvl: EntityLevel, posToCheck: Vector2f, discard: EntityLevel?) : EntityLevel? {
        var matcher: EntityLevel? = null
        lvl.traverseFunc {
            //Check for match, but make sure to not select itself

            val pos3 = it.getAbsolutePosition()
            val pos = Vector2f(pos3.x, pos3.y)

            if (pos.equals(posToCheck, 0.1f) && it != discard && it.hasComponent(AtomComponent::class)) {
                matcher = it
                return@traverseFunc
            }
        }
        return matcher
    }


    private fun addMolecule(placement: Mode.Placement) {
        val action = AtomCreationAction(placement.xPos, placement.yPos, placement.insert)
        action.runAction(workingState.molManager, workingState.level)
    }

    private fun replaceElement(replacement: Mode.Replacement) {
        val action = ElementalEditAction(replacement.atom, replacement.replacement)
        action.runAction(workingState.molManager, workingState.level)
    }


    private fun insertAtom(insert: AtomInsert, levelAtom: EntityLevel) : EntityLevel? {

        val action = AtomInsertionAction(insert, levelAtom)
        action.runAction(workingState.molManager, workingState.level)

        val toReturn = action.insertion

        return toReturn
    }


    private fun insertBondOrderChange(atomA: EntityLevel, atomB: EntityLevel) {
        val action = BondOrderAction(atomA, atomB)
        action.runAction(workingState.molManager, workingState.level)
    }


    override fun inProgress(): Boolean {
        return toolMode !is Mode.None
    }

    sealed class Mode {
        object None: Mode()
        data class Placement(val xPos: Float, val yPos: Float, val insert: AtomInsert) : Mode()
        data class Replacement(val atom: EntityLevel, val replacement: AtomInsert) : Mode()
        data class Dragging(val draggingAtom: EntityLevel, val stationaryAtom: EntityLevel, var proposedDragPos: Vector2f, var allowBondOrderChange: Boolean, var allowStateRestore: Boolean) : Mode()
    }

    companion object {
        private const val SIG_DELTA = 7.5f
    }
}
