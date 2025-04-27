package uk.co.jcox.chemvis.application

import com.fasterxml.jackson.databind.ser.impl.StringArraySerializer
import imgui.ImGui
import imgui.type.ImInt
import org.joml.Matrix4f
import org.joml.Vector2f
import org.joml.Vector3f
import org.joml.Vector4f
import org.joml.plus
import org.lwjgl.glfw.GLFW
import org.lwjgl.opengl.GL11
import org.openscience.cdk.Atom
import uk.co.jcox.chemvis.application.editor.CDKManager
import uk.co.jcox.chemvis.application.editor.IMoleculeManager
import uk.co.jcox.chemvis.cvengine.*
import java.io.File
import java.util.UUID

class ChemVis : IApplication, IEngineInput {

    private lateinit var camera: Camera2D
    private lateinit var program: ShaderProgram
    private lateinit var batcher: Batch2D
    private lateinit var textureManager: TextureManager
    private lateinit var font: BitmapFont

    private lateinit var services: ICVServices

    private var lastMouseX: Float = 0.0f
    private var lastMouseY: Float = 0.0f

    private val molManager: IMoleculeManager = CDKManager()

    private val tools = arrayOf("C", "O", "H")
    private val selectedTool: ImInt = ImInt(0)
    private var selectedAtom: Pair<UUID, UUID>? = null


    override fun init(cvServices: ICVServices) {

        this.services = cvServices

        services.setActiveInputHandler(this)

        val wm = services.windowMetrics()

        camera = Camera2D(wm.x, wm.y)

        program = ShaderProgram(services.loadShaderSourceResource(File("data/shaders/default2D.vert")), services.loadShaderSourceResource(File("data/shaders/default2D.frag")))
        program.init()
        program.validateProgram()
        batcher = Batch2D()


        program.bind()

        textureManager = TextureManager()
        this.textureManager.manageTexture("logo", services.loadTextureResource(File("data/textures/chemvis_logo.png")))
        this.textureManager.manageTexture("logo1", services.loadTextureResource(File("data/textures/texture1.png")));

        font = services.loadFontResource(File("data/fonts/sourceserif.ttf"), 140,
            "@ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789. ():", true, textureManager)


        GL11.glClearColor(0.22f, 0.22f, 0.22f, 1.0f)

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

        font.text("Font rendering Working :)", Vector3f(1.0f, 1.0f, 1.0f), batcher, program, 300.0f, 300.0f, 0.1f)


        //Tool setup
        ImGui.begin("Editor Settings")
        ImGui.combo("Tool Selection", selectedTool, tools)
        ImGui.end()


        //Check what atom is selected
        for (molecule in molManager.molecules()) {
            val rootPos = molManager.getMoleculePosition(molecule)
            for (atom in molManager.atoms(molecule)) {
                val offsetPos = molManager.getAtomOffsetPosition(atom)
                val atomPos = rootPos + offsetPos

                //Also check if the cursor is close enough to select it
                val locX = DoubleArray(1)
                val locY = DoubleArray(1)
                GLFW.glfwGetCursorPos(services.glfwEngineWindow(), locX, locY)

                val cursorPos = Vector4f(locX[0].toFloat(), locY[0].toFloat(), 0.0f, 1.0f)
                val cursorWorld = camera.screenToWorld(cursorPos)
                val atomPos4 = Vector4f(atomPos, 0.0f, 1.0f)

                val diff = atomPos4.sub(cursorWorld, Vector4f())
                if (diff.length() <= SELECTION_RADIUS) {
                    selectedAtom = Pair(molecule, atom)
                    break
                } else {
                    selectedAtom = null
                }
            }

            if (selectedAtom != null) {
                break
            }
        }



        //Render selection
        if (selectedAtom != null) {
            val molPos = molManager.getMoleculePosition(selectedAtom?.first)
            val atomOffset = molManager.getAtomOffsetPosition(selectedAtom?.second)
            val pos = molPos + atomOffset
            batcher.begin(GL11.GL_TRIANGLES)
            batcher.addBatch(Shaper2D.rectangle(pos.x, pos.y, SELECTION_MARKER_SIZE.toFloat(), SELECTION_MARKER_SIZE.toFloat()))
            batcher.end()
        }

        //Rendering atoms time!
        for (molecule in molManager.molecules()) {
            val rootPos = molManager.getMoleculePosition(molecule)
            //Step 1 - Get all the atoms in the molecule
            for (atom in molManager.atoms(molecule)) {

                val offsetPos = molManager.getAtomOffsetPosition(atom)
                val atomPos = rootPos + offsetPos

                font.text(molManager.getAtomSymbol(atom), Vector3f(1.0f, 1.0f, 1.0f), batcher, program, atomPos.x, atomPos.y, 0.1f)


            }
        }

    }

    override fun mouseScrollEvent(xScroll: Double, yScroll: Double) {
        //If CTRL is pressed down, and you scroll, zoom in:
        if (GLFW.glfwGetKey(services.glfwEngineWindow(), GLFW.GLFW_KEY_LEFT_CONTROL) != GLFW.GLFW_PRESS) {
            return;
        }

        this.camera.camWidth -= yScroll.toFloat()
    }


    override fun mouseMoveEvent(xpos: Double, ypos: Double) {


        if (GLFW.glfwGetMouseButton(services.glfwEngineWindow(), GLFW.GLFW_MOUSE_BUTTON_3) == GLFW.GLFW_RELEASE) {
            lastMouseX = xpos.toFloat()
            lastMouseY = ypos.toFloat()
        }

        //If middle mouse button is pressed and you move the mouse
        if (GLFW.glfwGetMouseButton(services.glfwEngineWindow(), GLFW.GLFW_MOUSE_BUTTON_3) != GLFW.GLFW_PRESS) {
            return;
        }

        val deltaX: Float = xpos.toFloat() - lastMouseX
        val deltaY: Float = ypos.toFloat() - lastMouseY
        lastMouseX = xpos.toFloat()
        lastMouseY = ypos.toFloat()

        val scale = 0.1f

        camera.cameraPosition.add(Vector3f(-deltaX * scale, deltaY * scale, 0.0f))
    }


    override fun mouseClickEvent(button: Int, action: Int, mods: Int) {
        if (button != GLFW.GLFW_MOUSE_BUTTON_1) {
            return;
        }

        if (action != GLFW.GLFW_PRESS) {
            return;
        }

        //Get location of the screen clicked
        val cursorX = DoubleArray(1)
        val cursorY = DoubleArray(1)
        GLFW.glfwGetCursorPos(services.glfwEngineWindow(), cursorX, cursorY)

        val clickPos = Vector4f(cursorX[0].toFloat(), cursorY[0].toFloat(), 0.0f, 1.0f)
        val worldSpace = camera.screenToWorld(clickPos)

        //Assuming clicked with the carbon skeleton tool create a new methane

        if (selectedAtom == null) {
            val methane = molManager.createMolecule()
            val carbon = molManager.addAtom(methane, tools[selectedTool.get()])
            molManager.setMoleculePosition(methane, Vector2f(worldSpace.x, worldSpace.y))
            molManager.setAtomOffsetPosition(carbon, Vector2f())
        } else {
            //Add a bond to the atom


        }

    }

    override fun cleanup() {
        this.program.close()
        this.batcher.close()
        this.textureManager.close()
    }

    companion object {
        val SELECTION_RADIUS = 55
        val SELECTION_MARKER_SIZE = 20;
    }
}