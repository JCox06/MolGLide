package uk.co.jcox.chemvis.application.moleditorstate.tool

import uk.co.jcox.chemvis.application.graph.LevelContainer
import uk.co.jcox.chemvis.application.moleditorstate.ActionManager
import uk.co.jcox.chemvis.application.moleditorstate.RingInsert
import uk.co.jcox.chemvis.application.moleditorstate.SelectionManager
import uk.co.jcox.chemvis.application.moleditorstate.action.RingCreatorAction
import uk.co.jcox.chemvis.application.ui.tool.CommonTemplateToolView
import uk.co.jcox.chemvis.cvengine.Camera2D
import uk.co.jcox.chemvis.cvengine.IRenderTargetContext
import uk.co.jcox.chemvis.cvengine.IResourceManager
import uk.co.jcox.chemvis.cvengine.InputManager


class CommonTemplateTool(commonTemplateToolView: CommonTemplateToolView, renderingContext: IRenderTargetContext, inputManager: InputManager, camera2D: Camera2D, levelContainer: LevelContainer, selectionManager: SelectionManager, actionManager: ActionManager) : Tool<CommonTemplateToolView>( commonTemplateToolView, renderingContext, inputManager, camera2D, levelContainer, selectionManager, actionManager) {



    override fun onClick(clickX: Float, clickY: Float) {

        val action = RingCreatorAction(clickX, clickY, toolViewUI.getTemplate(), selectionManager.primarySelection)
        actionManager.executeAction(action)

    }

    override fun onRelease(clickX: Float, clickY: Float) {

    }

    override fun update() {

    }
}