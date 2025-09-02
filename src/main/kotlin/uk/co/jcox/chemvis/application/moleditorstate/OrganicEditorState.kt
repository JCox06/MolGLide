package uk.co.jcox.chemvis.application.moleditorstate

import org.joml.Vector2f
import uk.co.jcox.chemvis.application.graph.LevelContainer
import uk.co.jcox.chemvis.application.graph.LevelRenderer
import uk.co.jcox.chemvis.cvengine.ApplicationState
import uk.co.jcox.chemvis.cvengine.Camera2D
import uk.co.jcox.chemvis.cvengine.ICVServices
import uk.co.jcox.chemvis.cvengine.IInputSubscriber
import uk.co.jcox.chemvis.cvengine.IRenderTargetContext
import uk.co.jcox.chemvis.cvengine.InputManager
import uk.co.jcox.chemvis.cvengine.RawInput

class OrganicEditorState (
    val services: ICVServices,
    val renderingContext: IRenderTargetContext,
    val renderer: LevelRenderer
) : ApplicationState(renderingContext), IInputSubscriber {

    private val level = LevelContainer()
    private val camera = Camera2D(renderingContext.getWidth().toInt(), renderingContext.getHeight().toInt())

    override fun init() {

    }

    override fun update(inputManager: InputManager, timeElapsed: Float) {

    }

    override fun render(viewport: Vector2f) {

    }

    override fun cleanup() {

    }

    override fun onPause() {
        services.inputs().unsubscribe(this)
    }

    override fun onResume() {
        services.inputs().subscribe(this)
    }

    override fun clickEvent(inputManager: InputManager, key: RawInput) {

    }

    override fun clickReleaseEvent(inputManager: InputManager, key: RawInput) {

    }

    override fun mouseMoveEvent(inputManager: InputManager, xPos: Double, yPos: Double) {

    }

    override fun mouseScrollEvent(inputManager: InputManager, xScroll: Double, yScroll: Double) {

    }


    fun mouseWorld() : Vector2f {
        val mousePos = renderingContext.getMousePos(services.inputs())
        return camera.screenToWorld(mousePos)
    }
}