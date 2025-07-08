package uk.co.jcox.chemvis.application.moleditor.tools

import org.checkerframework.checker.units.qual.m
import uk.co.jcox.chemvis.application.moleditor.ClickContext
import uk.co.jcox.chemvis.application.moleditor.CompoundInsert
import uk.co.jcox.chemvis.application.moleditor.Selection
import uk.co.jcox.chemvis.application.moleditor.ToolCreationContext
import uk.co.jcox.chemvis.application.moleditor.actions.CyclohexaneRingAction
import uk.co.jcox.chemvis.application.moleditor.actions.CyclopentaneRingAction
import uk.co.jcox.chemvis.application.moleditor.actions.CyclopropaneRingAction
import uk.co.jcox.chemvis.cvengine.scenegraph.EntityLevel

class TemplateTool(context: ToolCreationContext) : Tool(context) {

    /**
     * The tool will allow a template to be inserted if dragging is false.
     * After the insertion, if you continue to hold the mouse down, you can alter the orientation of the template
     * This sets dragging to true.
     */
    private var mode: Mode = Mode.None

    override fun update() {
        val currentMode = mode
        if (currentMode is Mode.Dragging) {
            applyRotations(currentMode.addedMolecule)
        }
    }

    override fun processClick(clickDetails: ClickContext) {
        if (mode is Mode.Dragging) {
            return
        }

        val selection = context.selectionManager.primarySelection

        if (selection is Selection.None) {
            //Form the isolated version of the rings
            when (clickDetails.compoundInsert) {
                CompoundInsert.BENZENE -> createNewCyclohexane(true)
                CompoundInsert.CYLCOHEXANE -> createNewCyclohexane(false)
                CompoundInsert.CYCLOPENATNE -> createNewCyclopentane()
                CompoundInsert.CYCLOPROPANE -> createNewCyclopropane()
            }
        }

        if (selection is Selection.Active) {
            //Then we need to fuse the ring to another molecule through a common bond
        }
    }

    override fun processClickRelease(clickDetails: ClickContext) {
        if (mode !is Mode.None) {
            mode = Mode.None
            pushChanges()
        }
    }

    override fun renderTransientUI(transientUI: EntityLevel) {

    }

    override fun inProgress(): Boolean {
        return mode !is Mode.None
    }


    private fun applyRotations(rootMoleculeNode: EntityLevel) {
        val angleOfRotationToAdd = context.inputManager.deltaMousePos()
        //TODO - Apply these rotations
    }

    private fun createNewCyclohexane(benzene: Boolean) {
        val mouseWorld = mouseWorld()
        val action = CyclohexaneRingAction(mouseWorld.x, mouseWorld.y, benzene)
        action.runAction(workingState.molManager, workingState.level)
        mode = Mode.Dragging(action.rootMolecule)
    }

    private fun createNewCyclopentane() {
        val mouseWorld = mouseWorld()
        val action = CyclopentaneRingAction(mouseWorld.x, mouseWorld.y)
        action.runAction(workingState.molManager, workingState.level)
        mode = Mode.Dragging(action.rootMolecule)

    }

    private fun createNewCyclopropane() {
        val mouseWorld = mouseWorld()
        val action = CyclopropaneRingAction(mouseWorld.x, mouseWorld.y)
        action.runAction(workingState.molManager, workingState.level)
        mode = Mode.Dragging(action.rootMolecule)
    }

    sealed class Mode {
        object None: Mode()
        data class Dragging(val addedMolecule: EntityLevel) : Mode()
    }
}