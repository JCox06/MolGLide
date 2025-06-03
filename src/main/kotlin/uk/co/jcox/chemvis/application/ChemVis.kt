package uk.co.jcox.chemvis.application

import org.joml.Vector3f
import org.lwjgl.opengl.GL11
import uk.co.jcox.chemvis.application.moleditor.OrganicEditorState
import uk.co.jcox.chemvis.cvengine.*
import java.io.File

class ChemVis : IApplication, IInputSubscriber {

    private lateinit var camera: Camera2D


    private lateinit var services: ICVServices

    private var lastMouseX: Float = 0.0f
    private var lastMouseY: Float = 0.0f

    private val fontSize = 140

    override fun init(cvServices: ICVServices) {

        this.services = cvServices
        services.resourceManager().loadFontFromDisc(FONT, File("data/chemvis/fonts/ubuntu.ttf"), CVEngine.STD_CHARACTER_SET, fontSize)
        val wm = services.windowMetrics()
        camera = Camera2D(wm.x, wm.y)


        GL11.glClearColor(0.22f, 0.22f, 0.22f, 1.0f)


        loadCoreAssets()

//        val state = TestState(services.levelRenderer(), camera)
        val state = OrganicEditorState(services.levelRenderer(), camera)

        services.setCurrentApplicationState(state)
        services.inputs().subscribe(state)
        services.inputs().subscribe(this)
    }

    override fun loop() {
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT or GL11.GL_DEPTH_BUFFER_BIT)

        val wm = services.windowMetrics()
        GL11.glViewport(0, 0, wm.x, wm.y)
        camera.update(wm.x, wm.y)


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

    override fun clickEvent(inputManager: InputManager, key: RawInput) {
        if (key == RawInput.KEY_P) {
            services.toggleDebugPanel()
        }
    }

    private fun loadCoreAssets() {
        val selectionMarkerMesh = Shaper2D.circle(0.0f, 0.0f, 1.0f)
        services.resourceManager().manageMesh(SELECTION_MARKER_MESH, selectionMarkerMesh)
    }

    override fun cleanup() {

    }

    companion object {
        const val FONT: String = "APP_FONT"
        const val GLOBAL_SCALE: Float = 0.1f
        const val SELECTION_MARKER_MESH: String = "SELECTION_MARKER"
    }
}