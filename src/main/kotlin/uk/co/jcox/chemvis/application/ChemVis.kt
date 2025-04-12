package uk.co.jcox.chemvis.application

import org.apache.jena.iri.IRIRelativize
import org.joml.Matrix4f
import org.joml.Vector2f
import org.joml.Vector3f
import org.joml.plus
import org.lwjgl.glfw.GLFW
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL30
import uk.co.jcox.chemvis.cvengine.*
import java.io.File

class ChemVis : IApplication, IEngineInput {

    private lateinit var camera: Camera2D
    private lateinit var program: ShaderProgram
    private lateinit var batcher: Batch2D
    private lateinit var textureManager: TextureManager
    private lateinit var font: BitmapFont

    private val methanes: MutableList<Vector2f> = mutableListOf()

    private var lastMouseX: Float = 0.0f
    private var lastMouseY: Float = 0.0f

    override fun init(engine: CVEngine) {

        engine.setActiveInputHandler(this)

        camera = Camera2D(engine.windowX(), engine.windowY())

        program = ShaderProgram(engine.loadShaderSourceResource(File("data/shaders/default2D.vert")), engine.loadShaderSourceResource(File("data/shaders/default2D.frag")))
        program.init()
        program.validateProgram()
        batcher = Batch2D()


        program.bind()

        textureManager = TextureManager()
        this.textureManager.manageTexture("logo", engine.loadTextureResource(File("data/textures/chemvis_logo.png")))
        this.textureManager.manageTexture("logo1", engine.loadTextureResource(File("data/textures/texture1.png")));

        font = engine.loadFontResource(File("data/fonts/sourceserif.ttf"), 140,
            "@ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789. ()", true, textureManager)


        GL11.glClearColor(0.22f, 0.22f, 0.22f, 1.0f)

    }

    override fun loop(engine: CVEngine) {
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT)
        val winWidth = engine.windowX()
        val winHeight = engine.windowY()
        GL11.glViewport(0, 0, winWidth, winHeight)
        camera.update(engine.windowX(), engine.windowY())

        //Drawing time
        program.uniform("uPerspective", camera.combined())
        program.uniform("uModel", Matrix4f())

        this.program.bind()

        font.text("Hello", Vector3f(1.0f, 1.0f, 1.0f), batcher, program, 300.0f, 300.0f, 0.2f)
    }

    override fun mouseScrollEvent(xScroll: Double, yScroll: Double) {
        //If CTRL is pressed down, and you scroll, zoom in:
        if (GLFW.glfwGetKey(GLFW.glfwGetCurrentContext(), GLFW.GLFW_KEY_LEFT_CONTROL) != GLFW.GLFW_PRESS) {
            return;
        }

        this.camera.camWidth -= yScroll.toFloat()
    }


    override fun mouseMoveEvent(xpos: Double, ypos: Double) {


        if (GLFW.glfwGetMouseButton(GLFW.glfwGetCurrentContext(), GLFW.GLFW_MOUSE_BUTTON_3) == GLFW.GLFW_RELEASE) {
            lastMouseX = xpos.toFloat()
            lastMouseY = ypos.toFloat()
        }

        //If middle mouse button is pressed and you move the mouse
        if (GLFW.glfwGetMouseButton(GLFW.glfwGetCurrentContext(), GLFW.GLFW_MOUSE_BUTTON_3) != GLFW.GLFW_PRESS) {
            return;
        }

        val deltaX: Float = xpos.toFloat() - lastMouseX
        val deltaY: Float = ypos.toFloat() - lastMouseY
        lastMouseX = xpos.toFloat()
        lastMouseY = ypos.toFloat()

        val scale = 0.1f

        camera.cameraPosition.add(Vector3f(-deltaX * scale, deltaY * scale, 0.0f))
    }

    override fun cleanup() {
        this.program.close()
        this.batcher.close()
        this.textureManager.close()
    }
}