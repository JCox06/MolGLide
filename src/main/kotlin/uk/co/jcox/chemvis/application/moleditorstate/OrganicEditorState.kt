package uk.co.jcox.chemvis.application.moleditorstate

import org.apache.jena.sparql.function.library.context
import org.apache.jena.sparql.function.library.print
import org.joml.Vector2f
import org.joml.Vector3f
import org.lwjgl.opengl.GL11
import uk.co.jcox.chemvis.application.graph.ChemAtom
import uk.co.jcox.chemvis.application.graph.ChemMolecule
import uk.co.jcox.chemvis.application.graph.LevelContainer
import uk.co.jcox.chemvis.application.graph.LevelRenderer
import uk.co.jcox.chemvis.application.moleditorstate.tool.AtomBondTool
import uk.co.jcox.chemvis.application.moleditorstate.tool.Tool
import uk.co.jcox.chemvis.application.moleditorstate.tool.ToolboxContext
import uk.co.jcox.chemvis.cvengine.ApplicationState
import uk.co.jcox.chemvis.cvengine.Camera2D
import uk.co.jcox.chemvis.cvengine.ICVServices
import uk.co.jcox.chemvis.cvengine.IInputSubscriber
import uk.co.jcox.chemvis.cvengine.IRenderTargetContext
import uk.co.jcox.chemvis.cvengine.InputManager
import uk.co.jcox.chemvis.cvengine.RawInput
import java.util.UUID

class OrganicEditorState (
    val services: ICVServices,
    val renderingContext: IRenderTargetContext,
    val renderer: LevelRenderer,
    val toolbox: ToolboxContext,
) : ApplicationState(renderingContext), IInputSubscriber {

    private val levelContainer = LevelContainer()
    private val camera = Camera2D(renderingContext.getWidth().toInt(), renderingContext.getHeight().toInt())
    private val selectionManager = SelectionManager()

    private val currentTool: Tool = AtomBondTool(toolbox, renderingContext, services.inputs(), camera, levelContainer, selectionManager)

    override fun init() {

    }

    override fun update(inputManager: InputManager, timeElapsed: Float) {
        camera.update(renderTargetContext.getWidth().toInt(), renderingContext.getHeight().toInt())

        if (!inputManager.mouseClick(RawInput.MOUSE_1)) {
            val mousePos = camera.screenToWorld(renderTargetContext.getMousePos(inputManager))
            selectionManager.update(levelContainer, mousePos.x, mousePos.y)
        }
    }

    override fun render(viewport: Vector2f) {
        GL11.glViewport(0, 0, renderTargetContext.getWidth().toInt(), renderTargetContext.getHeight().toInt())
        renderer.renderLevel(this.levelContainer, camera, viewport)
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
        val mousePosScreen = renderingContext.getMousePos(inputManager)

        if (key == RawInput.MOUSE_1) {
            val mousePos = camera.screenToWorld(mousePosScreen)

            currentTool.onClick(mousePos.x, mousePos.y)
        }
    }

    override fun clickReleaseEvent(inputManager: InputManager, key: RawInput) {
        if (key == RawInput.MOUSE_1) {
            val mousePos = camera.screenToWorld(renderingContext.getMousePos(inputManager))
            currentTool.onRelease(mousePos.x, mousePos.y)
        }
    }

    override fun mouseMoveEvent(inputManager: InputManager, xPos: Double, yPos: Double) {

        if (inputManager.mouseClick(RawInput.MOUSE_3)) {
            val delta = inputManager.deltaMousePos()

            val scale = 0.05f

            camera.cameraPosition.add(Vector3f(-delta.x * scale, delta.y * scale, 0.0f))
        }
    }

    override fun mouseScrollEvent(inputManager: InputManager, xScroll: Double, yScroll: Double) {
        if (inputManager.keyClick(RawInput.LCTRL)) {
            this.camera.camWidth -= yScroll.toFloat() * 2;
        }
    }

    companion object {
        const val ATOM_PLANE = -3.0f
    }
}