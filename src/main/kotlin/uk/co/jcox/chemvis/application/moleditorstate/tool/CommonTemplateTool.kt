package uk.co.jcox.chemvis.application.moleditorstate.tool

import uk.co.jcox.chemvis.application.graph.LevelContainer
import uk.co.jcox.chemvis.application.moleditorstate.ActionManager
import uk.co.jcox.chemvis.application.moleditorstate.SelectionManager
import uk.co.jcox.chemvis.application.moleditorstate.action.TemplateRingCreationAction
import uk.co.jcox.chemvis.cvengine.Camera2D
import uk.co.jcox.chemvis.cvengine.IRenderTargetContext
import uk.co.jcox.chemvis.cvengine.IResourceManager
import uk.co.jcox.chemvis.cvengine.InputManager

class CommonTemplateTool(toolboxContext: ToolboxContext, renderingContext: IRenderTargetContext, inputManager: InputManager, camera2D: Camera2D, levelContainer: LevelContainer, selectionManager: SelectionManager, actionManager: ActionManager) : Tool(toolboxContext, renderingContext, inputManager, camera2D, levelContainer, selectionManager, actionManager) {
    override fun onClick(clickX: Float, clickY: Float) {
        //Currently only a very simple tool. It will just insert new rings into the editor. Later it can append common rings to existing molecules.
        //This will require an update to the selection manager to check for a common bond
        //Depending on this either TemplateCreationAction is called or TemplateFuseAction

        val action = TemplateRingCreationAction(clickX, clickY, toolboxContext.templateInsert)
        actionManager.executeAction(action)
    }

    override fun onRelease(clickX: Float, clickY: Float) {

    }

    override fun renderTransients(resourceManager: IResourceManager) {

    }

    override fun update() {

    }
}