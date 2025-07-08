package uk.co.jcox.chemvis.application.moleditor.tools

import uk.co.jcox.chemvis.application.moleditor.ClickContext
import uk.co.jcox.chemvis.application.moleditor.CompoundInsert
import uk.co.jcox.chemvis.application.moleditor.Selection
import uk.co.jcox.chemvis.application.moleditor.ToolCreationContext
import uk.co.jcox.chemvis.application.moleditor.actions.CyclohexaneRingAction
import uk.co.jcox.chemvis.cvengine.scenegraph.EntityLevel

class TemplateTool(context: ToolCreationContext) : Tool(context) {

    /**
     * The tool will allow a template to be inserted if dragging is false.
     * After the insertion, if you continue to hold the mouse down, you can alter the orientation of the template
     * This sets dragging to true.
     */
    private var dragging = false

    override fun update() {

    }

    override fun processClick(clickDetails: ClickContext) {
        if (dragging) {
            return
        }

        dragging = true

        val selection = context.selectionManager.primarySelection

        if (selection is Selection.None) {
            //Form the isolated version of the rings
            when (clickDetails.compoundInsert) {
                CompoundInsert.BENZENE -> createNewCyclohexane(true)
                CompoundInsert.CYLCOHEXANE -> createNewCyclohexane(false)
                CompoundInsert.CYCLOPENATNE -> {}
                CompoundInsert.CYCLOPROPANE -> {}
            }
        }
    }

    override fun processClickRelease(clickDetails: ClickContext) {
        if (dragging) {
            dragging = false
            pushChanges()
        }
    }

    override fun renderTransientUI(transientUI: EntityLevel) {

    }

    override fun inProgress(): Boolean {
        return true
    }

    fun createNewCyclohexane(benzene: Boolean) {
        val mouseWorld = mouseWorld()
        val action = CyclohexaneRingAction(mouseWorld.x, mouseWorld.y, benzene)
        action.runAction(workingState.molManager, workingState.level)
    }
}