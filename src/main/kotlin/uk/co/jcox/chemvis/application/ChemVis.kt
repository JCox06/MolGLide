package uk.co.jcox.chemvis.application

import org.joml.Matrix4f
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL30
import uk.co.jcox.chemvis.cvengine.*
import java.io.File

class ChemVis : IApplication {

    private val SF = 50

    private val vertices = listOf(
        1.0f * SF,  1.0f * SF, -1.0f, 1.0f, 1.0f, 1.0f,  // top right
        1.0f * SF , -1.0f * SF, -1.0f, 1.0f, 0.0f, 1.0f,  // bottom right
        -1.0f * SF, -1.0f * SF, -1.0f, 0.0f, 0.0f, 1.0f,  // bottom left
        -1.0f * SF,  1.0f * SF, -1.0f, 0.0f, 1.0f, 1.0f   // top left
    )

    private val indices = listOf(
        0, 1, 3,   // first triangle
        1, 2, 3    // second triangle
    )

    private val vertices2 = listOf(
        (1.0f +5) * SF,  (1.0f + 5) * SF, -1.0f, 1.0f, 1.0f, 0.0f,  // top right
        (1.0f + 5) * SF , (-1.0f + 5) * SF, -1.0f, 1.0f, 0.0f, 0.0f,  // bottom right
        (-1.0f + 5) * SF, (-1.0f + 5) * SF, -1.0f, 0.0f, 0.0f, 0.0f,  // bottom left
        (-1.0f + 5) * SF,  (1.0f + 5) * SF, -1.0f, 0.0f, 1.0f, 0.0f   // top left
    )

    private val indices2 = listOf(
        0, 1, 3,   // first triangle
        1, 2, 3    // second triangle
    )

    private lateinit var camera: Camera2D
    private lateinit var program: ShaderProgram
    private lateinit var batcher: Batch2D
    private lateinit var textureManager: TextureManager

    override fun init(engine: CVEngine) {
        camera = Camera2D(engine.windowX(), engine.windowY())

        program = ShaderProgram(engine.loadShaderSourceResource(File("data/shaders/default2D.vert")), engine.loadShaderSourceResource(File("data/shaders/default2D.frag")))
        program.init()
        program.validateProgram()
        batcher = Batch2D()


        program.bind()
        batcher.mapProgramTextures(program)
        program.uniform("myText", 0)

        textureManager = TextureManager()
        this.textureManager.manageTexture("logo", engine.loadTextureResource(File("data/textures/chemvis_logo.png")))
        this.textureManager.manageTexture("logo1", engine.loadTextureResource(File("data/textures/texture1.png")));

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
        this.batcher.begin(GL11.GL_TRIANGLES)
//
        this.textureManager.useTexture("logo", GL30.GL_TEXTURE0)
        this.textureManager.useTexture("logo1", GL30.GL_TEXTURE1)
        this.batcher.addBatch(vertices, indices)
        this.batcher.addBatch(vertices2, indices2)
//
//
        this.batcher.end()

    }

    override fun cleanup() {
        this.program.close()
        this.batcher.close()
        this.textureManager.close()
    }
}