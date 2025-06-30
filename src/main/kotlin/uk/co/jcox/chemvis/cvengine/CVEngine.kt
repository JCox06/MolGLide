package uk.co.jcox.chemvis.cvengine

import imgui.ImFontConfig
import imgui.ImGui
import imgui.ImVec2
import imgui.flag.ImGuiCond
import imgui.flag.ImGuiConfigFlags
import imgui.gl3.ImGuiImplGl3
import imgui.glfw.ImGuiImplGlfw
import org.joml.Vector2f
import org.joml.Vector2i
import org.lwjgl.glfw.Callbacks
import org.lwjgl.glfw.GLFW
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL30
import org.lwjgl.opengl.GL43
import org.lwjgl.opengl.GLDebugMessageCallback
import org.lwjgl.system.Callback
import org.tinylog.Logger
import java.io.File
import java.lang.AutoCloseable
import kotlin.math.max

class CVEngine(private val name: String) : ICVServices, AutoCloseable {
    private val lwjglErrorCallback: Callback? = null
    private var windowHandle: Long = 0

    private lateinit var openGlImGui: ImGuiImplGl3
    private lateinit var glfwImGui: ImGuiImplGlfw
    private lateinit var inputManager: InputManager
    private lateinit var batcher: Batch2D
    private lateinit var instancer: InstancedRenderer
    private lateinit var resourceManager: IResourceManager
    private lateinit var levelRenderer: LevelRenderer


    private val appRenderStates: MutableMap<String?, ApplicationState> = mutableMapOf()

    private val pendingRenderStateChanges = mutableListOf<Pair<String?, ApplicationState>>()

    private val pendingRenderStateRemove = mutableListOf<String>()

    private var callback: GLDebugMessageCallback? = null


    private val viewport = Vector2f()

    private fun init() {
        Logger.info{"Starting CV3D Engine..."}

        GLFW.glfwSetErrorCallback { code: Int, desc: Long ->
            Logger.error { "[GLFW ERROR] " + code + GLFWErrorCallback.getDescription(desc) }
        }

        check(GLFW.glfwInit()) { "Could not init GLFW" }

        //Set render hints
        GLFW.glfwDefaultWindowHints()
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3)
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 3)
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_FORWARD_COMPAT, GLFW.GLFW_TRUE)
        GLFW.glfwWindowHint(GLFW.GLFW_DECORATED, GLFW.GLFW_TRUE)

        this.windowHandle = GLFW.glfwCreateWindow(800, 600, name, 0, 0)

        if (this.windowHandle == 0L) {
            throw RuntimeException("Filed to create a window and setup OpenGL")
        }

        //Setup window
        GLFW.glfwMakeContextCurrent(this.windowHandle)
        GLFW.glfwSwapInterval(1)
        GL.createCapabilities()


        GL43.glEnable(GL43.GL_DEBUG_OUTPUT)
        GL43.glEnable(GL43.GL_DEBUG_OUTPUT_SYNCHRONOUS)

        callback = GLDebugMessageCallback.create { source: Int, type: Int, id: Int, severity: Int, length: Int, message: Long, userParam: Long ->
            if (type == GL43.GL_DEBUG_TYPE_ERROR) {
                Logger.error { "[OpenGL ERROR] " + GLDebugMessageCallback.getMessage(length, message) }
            }
        }
        GL43.glDebugMessageCallback(callback, 0)

        GL11.glClearColor(0.02f, 0.02f, 0.02f, 1.0f)

        initialiseCoreServices()
        batcher.setup()
        instancer.setup()
        initialiseIntegratedResources()


        GL11.glEnable(GL11.GL_DEPTH_TEST)

        setupImGui()

        Logger.info{"GLFW and OpenGL have successfully started"}

    }


    private fun initialiseCoreServices() {
        Logger.info{"Initialising CV3D core services..."}
        this.inputManager = GLFWInputManager(windowHandle)
        this.batcher = Batch2D()
        this.instancer = InstancedRenderer()
        this.resourceManager = ResourceManager()
        this.levelRenderer = LevelRenderer(batcher, instancer, resourceManager)
        Logger.info{"Success! InputManager, Batcher, Instancer, ResourceManager, and LevelRenderer have all started"}
    }


    private fun initialiseIntegratedResources() {
        Logger.info{"Loading integrated resources..."}
        this.resourceManager.loadShadersFromDisc(SHADER_SIMPLE_TEXTURE, File("data/integrated/shaders/simpleTexture.vert"), File("data/integrated/shaders/simpleTexture.frag"), null)
        this.resourceManager.loadShadersFromDisc(SHADER_INSTANCED_LINE, File("data/integrated/shaders/instanceLine.vert"), File(("data/integrated/shaders/instanceLine.frag")), File("data/integrated/shaders/instanceLine.geom"))

        this.resourceManager.manageMesh(MESH_HOLDER_LINE, Shaper2D.line(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f), instancer)
    }


    private fun setupImGui() {
        ImGui.createContext()
        ImGui.styleColorsDark()

        val style = ImGui.getStyle()
        style.windowTitleAlign = ImVec2(0.5f, 0.5f)
        style.windowBorderSize = 1.0f
        style.childBorderSize = 1.0f
        style.frameBorderSize = 1.0f
        style.tabBorderSize = 1.0f
        style.frameRounding = 2.0f
        style.scrollbarRounding = 0.0f
        style.tabRounding = 0.0f
        style.windowRounding = 5.0f

        glfwImGui = ImGuiImplGlfw()
        openGlImGui = ImGuiImplGl3()

        val io = ImGui.getIO()
        io.fonts.setFreeTypeRenderer(true)

        val x = FloatArray(1)
        val y = FloatArray(1)
        GLFW.glfwGetWindowContentScale(this.windowHandle, x, y)
        val scale = x[0] * 18


//        io.addConfigFlags(ImGuiConfigFlags.ViewportsEnable)
        io.addConfigFlags(ImGuiConfigFlags.DockingEnable)
        io.configViewportsNoDecoration = false
        io.configViewportsNoTaskBarIcon = false
        io.configWindowsMoveFromTitleBarOnly = true

        val experience = io.fonts.addFontFromFileTTF("data/integrated/fonts/Roboto-Black.ttf", scale)
        io.fontDefault = experience

        glfwImGui.init(this.windowHandle, true)
        openGlImGui.init()
    }

    fun run(application: IApplication) {
        Logger.info("Ready, initialising application: {}", name)
        init()
        application.init(this)


        Logger.info{"Engine startup successful! Entering main render loop"}

        while (!GLFW.glfwWindowShouldClose(this.windowHandle)) {
            if (inputManager.keyClick(RawInput.KEY_Q) && inputManager.keyClick(RawInput.LCTRL)) {
                shutdown()
            }

            inputManager.update()
            glfwImGui.newFrame()
            openGlImGui.newFrame()
            ImGui.newFrame()

            //Always run the main application first
            application.loop()

            //Then check if the current state needs running
//            if (ImGui.getIO().wantCaptureMouse) {
//                inputManager.blockInput(true)
//            } else {
//                inputManager.blockInput(false)
//            }

//            if (currentState != null) {
//                currentState!!.update(inputManager, GLFW.glfwGetTime().toFloat())
//                currentState!!.render(viewport)
//            }


            renderAndUpdateStates()

            ImGui.render()
            openGlImGui.renderDrawData(ImGui.getDrawData())


            //ImGui flags
            if (ImGui.getIO().hasConfigFlags(ImGuiConfigFlags.ViewportsEnable)) {
                val toRestore = GLFW.glfwGetCurrentContext()
                ImGui.updatePlatformWindows()
                ImGui.renderPlatformWindowsDefault()
                GLFW.glfwMakeContextCurrent(toRestore)
            }


            GLFW.glfwSwapBuffers(this.windowHandle)
            GLFW.glfwPollEvents()
        }
        application.cleanup()
    }


    private fun renderAndUpdateStates() {
        appRenderStates.forEach { targetID, state ->

            if (targetID == null) {

                GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0)
            } else {
                val customTarget = resourceManager.getRenderTarget(targetID)
                GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, customTarget.frameBuffer)
            }


            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT or GL11.GL_DEPTH_BUFFER_BIT)


            if (!state.paused) {
                state.update(inputManager, GLFW.glfwGetTime().toFloat())
            }

            state.render(viewport)

            GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0)
        }


        //Apply pending changes for addition

        pendingRenderStateChanges.forEach { pair ->
            val currentState = appRenderStates[pair.first]

            if (currentState != null) {
                if (currentState is IInputSubscriber) {
                    this.inputManager.unsubscribe(currentState as IInputSubscriber)

                    currentState.cleanup()
                }
            }
            pair.second.init()
            pair.second.resume()
            appRenderStates[pair.first] = pair.second
        }

        pendingRenderStateChanges.clear()


        //Apply pending changes for removal

        pendingRenderStateRemove.forEach { stateID ->
            val currentState = appRenderStates[stateID]

            if (currentState == null) {
                return
            }

            if (currentState is IInputSubscriber) {
                this.inputManager.unsubscribe(currentState)
            }

            appRenderStates.remove(stateID)

            resourceManager.destroyRenderTarget(stateID)

            currentState.cleanup()
        }
    }


    override fun windowMetrics(): Vector2i {
        val width = IntArray(1)
        val height = IntArray(1)
        GLFW.glfwGetWindowSize(this.windowHandle, width, height)
        return Vector2i(width[0], height[0])
    }


    override fun setApplicationState(state: ApplicationState, renderTarget: String?) {
        pendingRenderStateChanges.add(Pair(renderTarget, state))
    }

    override fun shutdown() {
        GLFW.glfwSetWindowShouldClose(this.windowHandle, true)
    }

    override fun inputs(): InputManager {
        return inputManager
    }

    override fun batchRenderer(): Batch2D {
        return this.batcher
    }

    override fun levelRenderer(): LevelRenderer {
        return levelRenderer
    }

    override fun resourceManager(): IResourceManager {
        return this.resourceManager
    }


    override fun setViewport(a: Int, b: Int, c: Int, d: Int) {
        viewport.x = c.toFloat()
        viewport.y = d.toFloat()
        GL11.glViewport(a, b, c, d)
    }


    override fun instancedRenderer(): InstancedRenderer {
        return this.instancer
    }

    override fun close() {
        Logger.info{"====SHUTTING-DOWN===="}

        batcher.close()
        instancer.close()
        resourceManager.destroy()

        this.lwjglErrorCallback?.close()

        this.callback?.close()

        openGlImGui.shutdown()
        //this causes null pointer exception and I don't know why
        //According to the documentation I have done everything correctly ?
//        glfwImGui?.shutdown();
        ImGui.destroyContext()

        GLFW.glfwSetErrorCallback(null)?.free()

        Callbacks.glfwFreeCallbacks(this.windowHandle)
        GLFW.glfwDestroyWindow(this.windowHandle)
        GLFW.glfwTerminate()

        Logger.info{"Shut down successfully"}
    }

    override fun pauseAppState(stateID: String) {
        val actualState = appRenderStates[stateID]
        actualState?.pause()
    }

    override fun resumeAppState(stateID: String) {
        val actualState = appRenderStates[stateID]
        actualState?.resume()
    }


    override fun getAppStateRenderingContext(stateID: String): IRenderTargetContext? {
        return appRenderStates[stateID]?.renderTargetContext
    }


    override fun destroyAppState(stateID: String) {
        //State ID cannot be null because that would be destroying the main global state/only state

        pendingRenderStateRemove.add(stateID)
    }


    override fun getState(stateID: String): ApplicationState? {
        return appRenderStates[stateID]
    }

    companion object {
        const val SHADER_SIMPLE_TEXTURE: String = "integrated/simple_font"
        const val SHADER_INSTANCED_LINE: String = "integrated/simple_line"

        const val STD_CHARACTER_SET: String = "@!?- ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz12345678"

        const val MESH_HOLDER_LINE: String = "integrated/unit_line"


        //[3 float pos] [2 float texture] = 5 floats
        //Application-Wide standard vertex set
        const val VERTEX_SIZE = 5
        const val VERTEX_SIZE_BYTES = 5 * Float.SIZE_BYTES
    }
}
