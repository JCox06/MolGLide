package uk.co.jcox.chemvis.application.moleditor

import uk.ac.ebi.beam.Atom
import uk.co.jcox.chemvis.application.moleditor.actions.AtomCreationAction
import uk.co.jcox.chemvis.application.moleditor.actions.ElementalEditAction
import uk.co.jcox.chemvis.cvengine.scenegraph.EntityLevel

class NewAtomBondTool(context: ToolCreationContext) : Tool(context){

    private var toolMode: Mode = Mode.None

    override fun update() {

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

            is Mode.Placement -> addMolecule(mode)

            is Mode.Replacement -> replaceElement(mode)
        }
    }

    override fun processClickRelease(clickDetails: ClickContext) {

        //If the mode is something other than none, then commit the local stack to the main stack
        if (toolMode !is Mode.None) {
            toolMode = Mode.None

            pushChanges()
        }
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
    //If we should, then it will handle the switch
    private fun checkSwitchToInsertion() {

    }


    //Each mode is associated with an action
    //The following methods are responsible for creation the appropriate actions

    private fun addMolecule(placement: Mode.Placement) {
        val action = AtomCreationAction(placement.xPos, placement.yPos, placement.insert)
        action.runAction(workingState.molManager, workingState.level)
    }

    private fun replaceElement(replacement: Mode.Replacement) {
        val action = ElementalEditAction(replacement.atom, replacement.replacement)
        action.runAction(workingState.molManager, workingState.level)
    }

    sealed class Mode {
        object None: Mode()
        data class Placement(val xPos: Float, val yPos: Float, val insert: AtomInsert) : Mode()
        data class Replacement(val atom: EntityLevel, val replacement: AtomInsert) : Mode()
    }
}
