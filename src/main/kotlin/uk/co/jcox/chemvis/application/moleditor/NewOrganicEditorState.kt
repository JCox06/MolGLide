package uk.co.jcox.chemvis.application.moleditor


import org.joml.Vector2f
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
    private val camera2D: Camera2D,
    private val levelRenderer: LevelRenderer,
) : IApplicationState, IInputSubscriber {

    private val workState = WorkState()
    private val ui = ApplicationUI()
    private val selection = SelectionManager()

    private var moformula = "null"

    private lateinit var atomBondTool: Tool

    override fun init() {
        workState.init()

        atomBondTool = AtomBondTool(ToolCreationContext(workState, services.inputs(), selection, camera2D))

        atomBondTool.onCommit {
            workState.makeCheckpoint(it.clone())
        }

    }

    override fun update(inputManager: InputManager, timeElapsed: Float) {
        if (! inputManager.mouseClick(RawInput.MOUSE_1)) {
            val mousePos = camera2D.screenToWorld(inputManager.mousePos())
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

        levelRenderer.renderLevel(transientUI, camera2D, viewport)

        if (atomBondTool.actionInProgress) {
            levelRenderer.renderLevel(atomBondTool.workingState.level, camera2D, viewport)
        } else {
            levelRenderer.renderLevel(workState.get().level, camera2D, viewport)
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
            val mousePos = camera2D.screenToWorld(inputManager.mousePos())
            atomBondTool.processClick(ClickContext(mousePos.x, mousePos.y, ui.getActiveElement()))
        }

    }

    override fun clickReleaseEvent(inputManager: InputManager, key: RawInput) {

        if (key == RawInput.MOUSE_1) {
            val mousePos = camera2D.screenToWorld(inputManager.mousePos())
            atomBondTool.processClickRelease(ClickContext(mousePos.x, mousePos.y, ui.getActiveElement()))
        }
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