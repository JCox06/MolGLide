package uk.co.jcox.chemvis.application.moleditor.tools

import org.joml.Vector2f
import org.joml.Vector3f
import org.joml.minus
import org.joml.plus
import uk.co.jcox.chemvis.application.MolGLide
import uk.co.jcox.chemvis.application.moleditor.AtomComponent
import uk.co.jcox.chemvis.application.moleditor.AtomInsert
import uk.co.jcox.chemvis.application.moleditor.ClickContext
import uk.co.jcox.chemvis.application.moleditor.GhostImplicitHydrogenGroupComponent
import uk.co.jcox.chemvis.application.moleditor.MolIDComponent
import uk.co.jcox.chemvis.application.moleditor.OrganicEditorState
import uk.co.jcox.chemvis.application.moleditor.Selection
import uk.co.jcox.chemvis.application.moleditor.ToolCreationContext
import uk.co.jcox.chemvis.application.moleditor.actions.AtomCreationAction
import uk.co.jcox.chemvis.application.moleditor.actions.AtomInsertionAction
import uk.co.jcox.chemvis.application.moleditor.actions.BondOrderAction
import uk.co.jcox.chemvis.application.moleditor.actions.ElementalEditAction
import uk.co.jcox.chemvis.cvengine.scenegraph.EntityLevel
import uk.co.jcox.chemvis.cvengine.scenegraph.TextComponent
import uk.co.jcox.chemvis.cvengine.scenegraph.TransformComponent

class AtomBondTool(context: ToolCreationContext) : Tool(context){

    private var toolMode: Mode = Mode.None

    override fun update() {
        checkSwitchToInsertionMode()
        updateDraggingPosition()
        checkSwitchToBondJoin()
        checkRevertBondJoin()

        autoMoveGhostGroups()
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
            is Selection.None -> return Mode.Placement(clickDetails.xPos, clickDetails.yPos, clickDetails.atomInsert)

            is Selection.Active -> {
                val selectedID = selector.id
                val selectedEntity = workingState.level.findByID(selectedID)
                if (selectedEntity == null) {
                    return Mode.None
                }
                return Mode.Replacement(selectedEntity, clickDetails.atomInsert)
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

        //To make triangles, diagonal lines need to be shorter. So check if a line is straight (parallel to the coordinate axes)

        val lineDirection = tool.proposedDragPos - Vector2f(stationaryPos.x, stationaryPos.y)
        lineDirection.normalize().absolute()


        //Todo - This code will allow 3-membered rings possible - But I think calling it here is wrong, so I have stopped it
//        if (lineDirection.equals(Vector2f(1.0f, 0.0f), 0.001f) || lineDirection.equals(Vector2f(0.0f, 1.0f), 0.001f)) {
//            val newPos = closestPointToCircleCircumference(Vector2f(stationaryPos.x, stationaryPos.y), mouse, OrganicEditorState.Companion.CONNECTION_DIST)
//            tool.proposedDragPos = newPos
//        } else {
//            //Line is not on coordinate axes (needs to be longer)
//            val newPos = closestPointToCircleCircumference(Vector2f(stationaryPos.x, stationaryPos.y), mouse, OrganicEditorState.Companion.CONNECTION_DIST_ANGLE)
//            tool.proposedDragPos = newPos
//        }

        val newPos = closestPointToCircleCircumference(Vector2f(stationaryPos.x, stationaryPos.y), mouse, OrganicEditorState.Companion.CONNECTION_DIST)
        tool.proposedDragPos = newPos

        //Apply the proposed position
        val draggingTrans = tool.draggingAtom.getAbsoluteTranslation()
        val localTrans = Vector3f(tool.proposedDragPos, OrganicEditorState.Companion.XY_PLANE) - draggingTrans

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
            //To fix this, direct workstate access needs to be arranged to query the data in a lower workstate level
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


    private fun autoMoveGhostGroups() {

        val tool = toolMode

        if (tool !is Mode.Dragging) {
            return
        }

        //Move the ghost groups on the dragging atom and the stationary atom
        var draggingGhostGroup: EntityLevel? = null
        var stationaryGhostGroup: EntityLevel? = null

        //Find dragging ghost group
        tool.draggingAtom.traverseFunc {
            if (it.hasComponent(GhostImplicitHydrogenGroupComponent::class)) {
                draggingGhostGroup = it
                return@traverseFunc
            }
        }

        //Find Stationary ghost group
        tool.stationaryAtom.traverseFunc {
            if (it.hasComponent(GhostImplicitHydrogenGroupComponent::class)) {
                stationaryGhostGroup = it
                return@traverseFunc
            }
        }

        val dragGhost = draggingGhostGroup
        val statGhost = stationaryGhostGroup

        if (dragGhost == null || statGhost == null) {
            return
        }

        autoMoveGhostGroup(tool.draggingAtom, dragGhost, tool.stationaryAtom)
        autoMoveGhostGroup(tool.stationaryAtom, statGhost, tool.draggingAtom)
    }


    private fun autoMoveGhostGroup(atom: EntityLevel, ghostGroup: EntityLevel, otherAtom: EntityLevel) {
        //Get everything in terms of local pos
        val atomWithGhostPos = atom.getAbsolutePosition()
        val otherAtomPos = otherAtom.getAbsolutePosition()

        //Find the distance between the different ghost group positions and the other atom position
        //The largest distance will be the better option, as this will be further away

        val ghostPosTest1 = atomWithGhostPos + GHOST_GROUP_A
        val ghostPosTest2 = atomWithGhostPos + GHOST_GROUP_B

        val test1 = otherAtomPos.distance(ghostPosTest1)
        val test2 = otherAtomPos.distance(ghostPosTest2)

        if (test1 == test2) {
            //If they are the same distance - Then do nothing
            return
        }

        //Otherwise move the ghost group to the site that is shorter

        val ghostPosTrans = ghostGroup.getComponent(TransformComponent::class)
        val ghostText = ghostGroup.getComponent(TextComponent::class)

        if (test1 < test2) {
            //If test 1 is shorter, then move group to test 2 site
            //The test two site, starting from the left of the group, needs to take into account the size of the text as well
            ghostPosTrans.x = -OrganicEditorState.INLINE_DIST - ghostText.text.length * MolGLide.GLOBAL_SCALE * MolGLide.FONT_SIZE *1/3f
        }

        if (test1 > test2) {
            //If test 2 is shorter, then move group to test 1 site
            ghostPosTrans.x = OrganicEditorState.INLINE_DIST
        }
    }



    private fun checkIsHeteroBond(tool: Mode.Dragging): Boolean {
        val levelStationary = tool.stationaryAtom
        val levelDragging = tool.draggingAtom

        val structStationary  = levelStationary.getComponent(MolIDComponent::class)
        val structDragging = levelDragging.getComponent(MolIDComponent::class)

        val stationarySymbol = workingState.molManager.getSymbol(structStationary.molID)
        val draggingSymbol = workingState.molManager.getSymbol(structDragging.molID)

        return stationarySymbol != draggingSymbol
    }


    private fun sumOfVectorDirectionsFromAtom(atom: EntityLevel) : Vector3f {
        val atomComp = atom.getComponent(AtomComponent::class)

        val vectorSum = Vector3f()

        for (entityID in atomComp.connectedEntities) {
            val entity = workingState.level.findByID(entityID)
            entity?.let { vectorSum.add(it.getAbsolutePosition() - atom.getAbsolutePosition()) }
        }
        return vectorSum
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
        private val GHOST_GROUP_A = Vector3f(OrganicEditorState.INLINE_DIST, 0.0f, OrganicEditorState.XY_PLANE)
        private val GHOST_GROUP_B = Vector3f(-OrganicEditorState.INLINE_DIST, 0.0f, OrganicEditorState.XY_PLANE)
    }
}
