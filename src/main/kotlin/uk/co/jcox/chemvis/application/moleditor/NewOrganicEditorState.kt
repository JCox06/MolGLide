package uk.co.jcox.chemvis.application.moleditor


import org.joml.Vector2f
import org.joml.Vector3f
import uk.co.jcox.chemvis.cvengine.Camera2D
import uk.co.jcox.chemvis.cvengine.IApplicationState
import uk.co.jcox.chemvis.cvengine.ICVServices
import uk.co.jcox.chemvis.cvengine.IInputSubscriber
import uk.co.jcox.chemvis.cvengine.InputManager
import uk.co.jcox.chemvis.cvengine.LevelRenderer
import uk.co.jcox.chemvis.cvengine.RawInput
import uk.co.jcox.chemvis.cvengine.scenegraph.EntityLevel

class NewOrganicEditorState (
    private val services: ICVServices,
    private val levelRenderer: LevelRenderer,
) : IApplicationState, IInputSubscriber {

    private val workState = WorkState()
    private val ui = ApplicationUI()
    private val selection = SelectionManager()
    private val  camera = Camera2D(services.windowMetrics().x, services.windowMetrics().y)

    private var lastMouseX: Float = 0.0f
    private var lastMouseY: Float = 0.0f

    private var moformula = "null"

    private lateinit var atomBondTool: Tool

    override fun init() {
        workState.init()

        atomBondTool = AtomBondTool(ToolCreationContext(workState, services.inputs(), selection, camera))

        atomBondTool.onCommit {
            workState.makeCheckpoint(it.clone())
        }

    }

    override fun update(inputManager: InputManager, timeElapsed: Float) {

        camera.update(services.windowMetrics().x, services.windowMetrics().y)

        if (! inputManager.mouseClick(RawInput.MOUSE_1)) {
            val mousePos = camera.screenToWorld(inputManager.mousePos())
            selection.update(workState.get().level, mousePos.x, mousePos.y)
        }

        atomBondTool.update()

        val sel = selection.getPrimary()
        if (sel is Selection.Active) {
            val mol = sel.id
            val levelMol = workState.get().level.findByID(mol)
            val parent = levelMol?.parent
            val molIdComp = parent?.getComponent(MolIDComponent::class)

            if (molIdComp == null) {
                return
            }

            val formula = workState.get().molManager.getMolecularFormula(molIdComp.molID)
            moformula = formula
            return
        }

        moformula = "No molecule selected"
    }

    override fun render(viewport: Vector2f) {
        val transientUI = EntityLevel()

        atomBondTool.renderTransientUI(transientUI)

        levelRenderer.renderLevel(transientUI, camera, viewport)

        if (atomBondTool.actionInProgress) {
            levelRenderer.renderLevel(atomBondTool.workingState.level, camera, viewport)
        } else {
            levelRenderer.renderLevel(workState.get().level, camera, viewport)
        }

        ui.mainMenu(services, workState, atomBondTool, moformula)
        ui.renderWidgets()

    }


    override fun clickEvent(inputManager: InputManager, key: RawInput) {
        if (inputManager.keyClick(RawInput.LCTRL)) {
            if (key == RawInput.KEY_Z) {
                workState.undo()
                atomBondTool.refreshWorkingState()
            }
            if (key == RawInput.KEY_Y) {
                workState.redo()
                atomBondTool.refreshWorkingState()
            }
        }

        if (inputManager.mouseClick(RawInput.MOUSE_1)) {
            val mousePos = camera.screenToWorld(inputManager.mousePos())
            atomBondTool.processClick(ClickContext(mousePos.x, mousePos.y, ui.getActiveElement()))
        }

    }

    override fun clickReleaseEvent(inputManager: InputManager, key: RawInput) {

        if (key == RawInput.MOUSE_1) {
            val mousePos = camera.screenToWorld(inputManager.mousePos())
            atomBondTool.processClickRelease(ClickContext(mousePos.x, mousePos.y, ui.getActiveElement()))
        }
    }

    override fun mouseScrollEvent(inputManager: InputManager, xScroll: Double, yScroll: Double) {
        if (inputManager.keyClick(RawInput.LCTRL)) {
            this.camera.camWidth -= yScroll.toFloat() * 2;
        }
    }

    override fun mouseMoveEvent(inputManager: InputManager, xPos: Double, yPos: Double) {

        if (inputManager.mouseClick(RawInput.MOUSE_3)) {
            val deltaX: Float = xPos.toFloat() - lastMouseX
            val deltaY: Float = yPos.toFloat() - lastMouseY
            lastMouseX = xPos.toFloat()
            lastMouseY = yPos.toFloat()

            val scale = 0.5f

            camera.cameraPosition.add(Vector3f(-deltaX * scale, deltaY * scale, 0.0f))
        }

        lastMouseX = xPos.toFloat()
        lastMouseY = yPos.toFloat()
    }


    override fun cleanup() {

    }

    companion object {
        const val XY_PLANE = -1.0f
        const val SELECTION_RADIUS = 15.0f
        const val SELECTION_MARKER_SIZE = 10.0f
        const val INLINE_DIST = 10.0f
        const val CONNECTION_DIST = 35.0f
        const val BOND_WIDTH = 2.5f
        const val CARBON_IMPLICIT_LIMIT = 4
        const val DOUBLE_BOND_DISTANCE = 0.1f
    }
}