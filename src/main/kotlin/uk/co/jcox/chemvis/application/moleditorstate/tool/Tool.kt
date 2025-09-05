package uk.co.jcox.chemvis.application.moleditorstate.tool

import org.joml.Vector2f
import uk.co.jcox.chemvis.application.graph.LevelContainer
import uk.co.jcox.chemvis.application.moleditorstate.SelectionManager
import uk.co.jcox.chemvis.cvengine.Camera2D
import uk.co.jcox.chemvis.cvengine.IRenderTargetContext
import uk.co.jcox.chemvis.cvengine.InputManager

abstract class Tool(
    protected val toolboxContext: ToolboxContext,
    private val renderingContext: IRenderTargetContext,
    private val inputManager: InputManager,
    private val camera2D: Camera2D,
    protected val levelContainer: LevelContainer,
    protected val selectionManager: SelectionManager
) {


    abstract fun onClick(clickX: Float, clickY: Float)

    abstract fun onRelease(clickX: Float, clickY: Float)


    protected fun mouseWorld(): Vector2f {
        val mousePos = renderingContext.getMousePos(inputManager)
        return camera2D.screenToWorld(mousePos)
    }
}