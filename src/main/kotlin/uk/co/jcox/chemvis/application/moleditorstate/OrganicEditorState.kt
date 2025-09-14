package uk.co.jcox.chemvis.application.moleditorstate

import org.joml.Vector2f
import org.joml.Vector3f
import org.lwjgl.opengl.GL11
import uk.co.jcox.chemvis.application.MolGLide
import uk.co.jcox.chemvis.application.graph.LevelContainer
import uk.co.jcox.chemvis.application.graph.LevelRenderer
import uk.co.jcox.chemvis.application.moleditorstate.tool.AtomBondTool
import uk.co.jcox.chemvis.application.moleditorstate.tool.ImplicitAtomMoveTool
import uk.co.jcox.chemvis.application.moleditorstate.tool.Tool
import uk.co.jcox.chemvis.application.moleditorstate.tool.ToolboxContext
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
    val renderer: LevelRenderer,
    val toolbox: ToolboxContext,
) : ApplicationState(renderingContext), IInputSubscriber {

    val levelContainer = LevelContainer()
    private val actionManager = ActionManager(levelContainer)
    private val camera = Camera2D(renderingContext.getWidth().toInt(), renderingContext.getHeight().toInt())
    val selectionManager = SelectionManager()


    private var currentTool: Tool = AtomBondTool(toolbox, renderingContext, services.inputs(), camera, levelContainer, selectionManager, actionManager)

    override fun init() {

    }

    override fun update(inputManager: InputManager, timeElapsed: Float) {
        camera.update(renderTargetContext.getWidth().toInt(), renderingContext.getHeight().toInt())

        if (!inputManager.mouseClick(RawInput.MOUSE_1)) {
            val mousePos = camera.screenToWorld(renderTargetContext.getMousePos(inputManager))
            selectionManager.update(levelContainer, mousePos.x, mousePos.y)
        }

        currentTool.update()
    }

    override fun render(viewport: Vector2f) {
        GL11.glViewport(0, 0, renderTargetContext.getWidth().toInt(), renderTargetContext.getHeight().toInt())

        currentTool.renderTransients(services.resourceManager())

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

        //Check if CTRL key is being held down
        if (inputManager.keyClick(RawInput.LCTRL)) {
            if (key == RawInput.KEY_Z) {
                undo()
            }
            if (key == RawInput.KEY_Y) {
                redo()
            }
        }
    }

    fun undo() {
        actionManager.undoLastAction()
    }

    fun redo() {
        actionManager.restoreLastAction()
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

    fun getFormula() : String {
        val selection = selectionManager.primarySelection
        if (selection is SelectionManager.Type.Active) {
            val atom = selection.atom
            val molecule = atom.parent

            return levelContainer.chemManager.getMolecularFormula(molecule.molManagerLink)
        }
        return "No molecule selected"
    }

    fun useAtomBondTool() {
        currentTool = AtomBondTool(toolbox, renderingContext, services.inputs(), camera, levelContainer, selectionManager, actionManager)
    }

    fun useImplicitMoveTool() {
        currentTool = ImplicitAtomMoveTool(toolbox, renderingContext, services.inputs(), camera, levelContainer, selectionManager, actionManager)
    }

    companion object {
        const val ATOM_PLANE = -3.0f
        const val MARKER_PLANE = -2.0f
        const val CONNECTION_DISTANCE = 30.0f
        const val IMPLICIT_SCALE = 0.75f * MolGLide.FONT_SIZE * MolGLide.GLOBAL_SCALE

        const val MULTI_BOND_DISTANCE = 3.5f


        val COMMON_ANGLES = listOf<Float>(
            //Cardinal directions
            0.0f, 90.0f, -90.0f, 180.0f, -180.0f,

            //Semi Cardinal directions
            45.0f, 135.0f, -45.0f, -135.0f,

            //Odd angles - For triangles
            30.0f, -30.0f, 60.0f, -60.0f, 120.0f, -120.0f, 150.0f, -150.0f,

            //For Pentagons
            108.0f, -108.0f, 72.0f, -72.0f, 36.0f, -36.0f, 126.0f, -126.0f, 144.0f, -144.0f,
            18.0f, -18.0f, 162.0f, -162.0f, 126.0f, -126.0f, 36.0f, -36.0f, 54.0f, -54.0f,


            )
    }
}