package uk.co.jcox.chemvis.application

import org.joml.Matrix4f
import org.joml.Vector3f
import org.lwjgl.opengl.GL11
import uk.co.jcox.chemvis.application.states.EditorState
import uk.co.jcox.chemvis.application.states.TestState
import uk.co.jcox.chemvis.cvengine.*
import java.io.File

class ChemVis : IApplication, IInputSubscriber {

    private lateinit var camera: Camera2D
    private lateinit var font: BitmapFont

    private lateinit var services: ICVServices

    private lateinit var program: ShaderProgram;

    private var lastMouseX: Float = 0.0f
    private var lastMouseY: Float = 0.0f

    private val fontSize = 140

    override fun init(cvServices: ICVServices) {

        this.services = cvServices


        val wm = services.windowMetrics()

        camera = Camera2D(wm.x, wm.y)

        font = setupFont(fontSize)

        font.scale = 0.1f
        font.colour = Vector3f(1.0f, 1.0f, 1.0f)

        GL11.glClearColor(0.22f, 0.22f, 0.22f, 1.0f)

        program = services.programs().useProgram(ShaderProgramManager.SIMPLE_TEXTURE)

        val editorState = EditorState(services.renderer(), font, program, camera)
//        services.setCurrentApplicationState(editorState)
        services.setCurrentApplicationState(TestState())
        services.inputs().subscribe(editorState)
        services.inputs().subscribe(this)


    }

    override fun loop() {
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT)


        val wm = services.windowMetrics()
        GL11.glViewport(0, 0, wm.x, wm.y)
        camera.update(wm.x, wm.y)


        //Drawing time
        program.uniform("uPerspective", camera.combined())
        program.uniform("uModel", Matrix4f())


        this.program.bind()

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


    private fun setupFont(size: Int): BitmapFont {
        return services.loadFontResource(File("data/chemvis/fonts/ubuntu.ttf"), size,
            "@ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789. ():", true)
    }

    override fun cleanup() {

    }

}