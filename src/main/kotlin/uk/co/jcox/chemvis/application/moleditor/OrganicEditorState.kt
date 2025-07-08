package uk.co.jcox.chemvis.application.moleditor


import org.joml.Vector2f
import org.joml.Vector3f
import org.joml.Vector4f
import org.lwjgl.opengl.GL11
import uk.co.jcox.chemvis.application.MolGLide
import uk.co.jcox.chemvis.application.moleditor.tools.AtomBondTool
import uk.co.jcox.chemvis.application.moleditor.tools.TemplateTool
import uk.co.jcox.chemvis.application.moleditor.tools.Tool
import uk.co.jcox.chemvis.cvengine.ApplicationState
import uk.co.jcox.chemvis.cvengine.Camera2D
import uk.co.jcox.chemvis.cvengine.ICVServices
import uk.co.jcox.chemvis.cvengine.IInputSubscriber
import uk.co.jcox.chemvis.cvengine.InputManager
import uk.co.jcox.chemvis.cvengine.LevelRenderer
import uk.co.jcox.chemvis.cvengine.RawInput
import uk.co.jcox.chemvis.cvengine.IRenderTargetContext
import uk.co.jcox.chemvis.cvengine.scenegraph.EntityLevel
import uk.co.jcox.chemvis.cvengine.scenegraph.LineDrawerComponent
import uk.co.jcox.chemvis.cvengine.scenegraph.TextComponent
import uk.co.jcox.chemvis.cvengine.scenegraph.TransformComponent
import kotlin.math.pow
import kotlin.math.sqrt

class OrganicEditorState (
    private val services: ICVServices,
    renderTargetContext: IRenderTargetContext
) : ApplicationState(renderTargetContext), IInputSubscriber {

    private val workState = WorkState()
    private val selection = SelectionManager()
    private val camera = Camera2D(renderTargetContext.getWidth().toInt(), renderTargetContext.getHeight().toInt())
    private val levelRenderer: LevelRenderer = services.levelRenderer()


    private lateinit var clpCache: ChemLevelPair

    var readOnly = false

    var atomInsert = AtomInsert.CARBON
    var compoundInsert = CompoundInsert.BENZENE

    var moformula = "null"
    private set

    private lateinit var currentTool: Tool

    override fun init() {
        workState.init()

        refreshStateCache()

        setThemeStyle(Vector3f(1.0f, 1.0f, 1.0f), Vector3f(1.0f, 1.0f, 1.0f), 2.5f)

        setAtomBondTool()

        services.inputs().subscribe(this)
    }

    override fun onPause() {
        services.inputs().unsubscribe(this)
    }

    override fun onResume() {
        services.inputs().subscribe(this)
    }

    override fun update(inputManager: InputManager, timeElapsed: Float) {
        camera.update(renderTargetContext.getWidth().toInt(), renderTargetContext.getHeight().toInt())

        if (readOnly) {
            return
        }

        if (! inputManager.mouseClick(RawInput.MOUSE_1)) {
            val mousePos = camera.screenToWorld(renderTargetContext.getMousePos(inputManager))
            selection.update(clpCache.level, mousePos.x, mousePos.y)
        }


        currentTool.update()

        val sel = selection.getPrimary()
        if (sel is Selection.Active) {
            val selectedAtom = sel.id
            val levelAtom = clpCache.level.findByID(selectedAtom)

            if (levelAtom == null){
                return
            }

            val parentID = LevelViewUtil.getLvlMolFromLvlAtom(levelAtom)
            if (parentID  == null) {
                return
            }

            val parent = clpCache.level.findByID(parentID)
            val molIdComp = parent?.getComponent(MolIDComponent::class)

            if (molIdComp == null) {
                return
            }

            moformula = clpCache.molManager.getMolecularFormula(molIdComp.molID)
            return
        }

        moformula = "No molecule selected"
    }

    override fun render(viewport: Vector2f) {

        GL11.glViewport(0, 0, renderTargetContext.getWidth().toInt(), renderTargetContext.getHeight().toInt())

        val transientUI = EntityLevel()

        currentTool.renderTransientUI(transientUI)


        val textEntity = transientUI.addEntity()

        textEntity.addComponent(TransformComponent(0.0f, 20.0f, 0.0f))

        levelRenderer.renderLevel(transientUI, camera, viewport)

        if (currentTool.inProgress()) {
            levelRenderer.renderLevel(currentTool.workingState.level, camera, viewport)
        } else {
            levelRenderer.renderLevel(clpCache.level, camera, viewport)
        }
    }


    override fun clickEvent(inputManager: InputManager, key: RawInput) {
        if (readOnly) {
            return
        }

        if (inputManager.keyClick(RawInput.LCTRL)) {
            if (key == RawInput.KEY_Z) {
                undo()
            }
            if (key == RawInput.KEY_Y) {
                redo()
            }
        }

        if (inputManager.mouseClick(RawInput.MOUSE_1)) {
            val mousePos = camera.screenToWorld(renderTargetContext.getMousePos(inputManager))
            currentTool.processClick(ClickContext(mousePos.x, mousePos.y, atomInsert, compoundInsert))
        }

    }


    fun undo() {
        workState.undo()
        currentTool.refreshWorkingState()
        refreshStateCache()
    }


    fun redo() {
        workState.redo()
        currentTool.refreshWorkingState()
        refreshStateCache()
    }

    override fun clickReleaseEvent(inputManager: InputManager, key: RawInput) {

        if (readOnly) {
            return
        }

        if (key == RawInput.MOUSE_1) {
            val mousePos = camera.screenToWorld(renderTargetContext.getMousePos(inputManager))
            currentTool.processClickRelease(ClickContext(mousePos.x, mousePos.y, atomInsert, compoundInsert))
        }
    }

    override fun mouseScrollEvent(inputManager: InputManager, xScroll: Double, yScroll: Double) {
        if (inputManager.keyClick(RawInput.LCTRL)) {
            this.camera.camWidth -= yScroll.toFloat() * 2;
        }
    }

    override fun mouseMoveEvent(inputManager: InputManager, xPos: Double, yPos: Double) {

        if (inputManager.mouseClick(RawInput.MOUSE_3)) {
            val delta = inputManager.deltaMousePos()

            val scale = 0.05f

            camera.cameraPosition.add(Vector3f(-delta.x * scale, delta.y * scale, 0.0f))
        }
    }



    fun makeCheckpoint() {
        workState.makeCheckpoint()
    }



    //todo - Styling should not be done in this class. And styling should not be applied to the scene graph
    //The scene graph really needs to be redesigned, and entities should be able to inherit from other entities
    //But inheritance should not be on a scene graph relation. AND, custom ThemeStyle classes should be added that are interacted by custom entities to apply the themes

    fun setThemeStyle(colourText: Vector3f, colourLine: Vector3f, width: Float) {
        workState.setTextTheme(TextComponent("", MolGLide.FONT, colourText.x, colourText.y, colourText.z, MolGLide.GLOBAL_SCALE))
        workState.setLineTheme(LineDrawerComponent(clpCache.level.id, clpCache.level.id, width, colourLine.x , colourLine.y, colourLine.z))
        refreshStateCache()
    }

    fun refreshStateCache() {
        this.clpCache = workState.get()
    }

    override fun cleanup() {

    }


    fun setAtomBondTool() {
        currentTool = AtomBondTool(ToolCreationContext(workState, services.inputs(), renderTargetContext, selection, camera))

        setPushFunc(currentTool)
    }


    fun setTemplateTool() {
        currentTool = TemplateTool(ToolCreationContext(workState, services.inputs(), renderTargetContext, selection, camera))

        setPushFunc(currentTool)
    }

    private fun setPushFunc(tool: Tool) {
        currentTool.onCommit {
            workState.makeCheckpoint(it.clone())

            //After each commit action get a copy of the clp so we dont have to clone it every frame
            refreshStateCache()
        }
    }


    companion object {
        const val XY_PLANE = -1.0f
        const val SELECTION_RADIUS = 15.0f
        const val SELECTION_MARKER_SIZE = 10.0f
        const val INLINE_DIST = 10.0f
        const val CONNECTION_DIST = 35.0f
        val CONNECTION_DIST_ANGLE = sqrt(2 * ((CONNECTION_DIST / 2).pow(2)))
        const val BOND_WIDTH = 2.5f
        const val CARBON_IMPLICIT_LIMIT = 4
        const val DOUBLE_BOND_DISTANCE = 0.1f
        const val SNAPPING_DISTANCE = 5.0f
    }
}