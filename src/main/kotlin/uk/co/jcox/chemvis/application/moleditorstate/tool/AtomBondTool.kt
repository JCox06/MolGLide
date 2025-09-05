package uk.co.jcox.chemvis.application.moleditorstate.tool

import uk.co.jcox.chemvis.application.graph.LevelContainer
import uk.co.jcox.chemvis.application.moleditorstate.ActionManager
import uk.co.jcox.chemvis.application.moleditorstate.SelectionManager
import uk.co.jcox.chemvis.application.moleditorstate.action.AtomCreationAction
import uk.co.jcox.chemvis.cvengine.Camera2D
import uk.co.jcox.chemvis.cvengine.IRenderTargetContext
import uk.co.jcox.chemvis.cvengine.IResourceManager
import uk.co.jcox.chemvis.cvengine.InputManager

class AtomBondTool (
    toolboxContext: ToolboxContext, renderingContext: IRenderTargetContext, inputManager: InputManager, camera2D: Camera2D, levelContainer: LevelContainer, selectionManager: SelectionManager,
    actionManager: ActionManager,
) : Tool(toolboxContext, renderingContext, inputManager, camera2D, levelContainer, selectionManager, actionManager){

    var toolMode: Mode = Mode.None

    override fun onClick(clickX: Float, clickY: Float) {
        toolMode = prepareCorrectMode(clickX, clickY)

        when (val mode = toolMode) {
            is Mode.None -> {}
            is Mode.MolCreation -> createNewMolecule(mode)
        }
    }

    override fun onRelease(clickX: Float, clickY: Float) {

    }


    override fun renderTransients(resourceManager: IResourceManager) {
        renderTransientSelectionMarker(resourceManager)
    }

    private fun createNewMolecule(molCreation: Mode.MolCreation) {
        val atomCreationAction = AtomCreationAction(molCreation.xPos, molCreation.yPos, toolboxContext.atomInsert)
        actionManager.executeAction(atomCreationAction)
    }


    private fun prepareCorrectMode(clickX: Float, clickY: Float) : Mode {
        val selectionType = selectionManager.primarySelection
        if (selectionType is SelectionManager.Type.None) {
            //No molecule is selected, therefore we can't add to a molecule, and instead
            //we can only create a new one
            return Mode.MolCreation(clickX, clickY)
        }

        return Mode.None
    }

    sealed class Mode {
        object None: Mode()
        class MolCreation(val xPos: Float, val yPos: Float): Mode()
    }
}