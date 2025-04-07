package uk.co.jcox.chemvis.application

import org.joml.Matrix4f
import org.joml.Vector2f
import org.joml.Vector3f
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
        program.uniform("uPerspective", camera.projection)
        program.uniform("uModel", Matrix4f())
//
        this.program.bind()
        this.program.uniform("mainTexture", 0)
        this.textureManager.useTexture("logo", GL30.GL_TEXTURE0)

        this.batcher.begin(GL11.GL_TRIANGLES)

        this.batcher.addBatch(Shaper2D.rectangle(200.0f, 200.0f, 100.0f, 100.0f))


        this.batcher.end()

        //Draw Hello World on screen
        font.text("ChemVis2 (C) 2025 Evaluation Copy", Vector3f(1.0f, 0.0f, 0.0f) ,batcher, program, 0.0f, 10.0f, 0.05f)
        font.text("Welcoming you to ChemVis2", Vector3f(1.0f, 1.0f, 1.0f), batcher, program, (camera.camWidth / 2 ) - 115, (camera.camHeight /2 ) - 30, 0.12f)

    }

    override fun mouseClickEvent(button: Int, action: Int, mods: Int) {
        if (action == GLFW.GLFW_PRESS) {
            val width = DoubleArray(1)
            val height = DoubleArray(1)

            GLFW.glfwGetCursorPos(GLFW.glfwGetCurrentContext(), width, height)
            val win = Vector2f(width[0].toFloat(), height[0].toFloat())
            println("Win X: ${win.x} Y: ${win.y}")

            val proj = camera.screenToView(win)
            println("World X: ${proj.x} Y: ${proj.y}")
        }
    }

    override fun cleanup() {
        this.program.close()
        this.batcher.close()
        this.textureManager.close()
    }
}