package uk.co.jcox.chemvis.cvengine;

import imgui.ImFont;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.flag.ImGuiConfigFlags;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import org.joml.Vector2i;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GLUtil;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.Callback;
import org.lwjgl.system.MemoryStack;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;


import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;


import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;

public class CVEngine implements ICVServices, AutoCloseable{
    private final String name;
    private Callback lwjglErrorCallback;
    private long windowHandle;

    private ImGuiImplGl3 openGlImGui;
    private ImGuiImplGlfw glfwImGui;

    private InputManager inputManager;

    private IApplicationState currentState;

    //Acts as the main renderer
    private Batch2D batcher;

    private IResourceManager resourceManager;

    private LevelRenderer levelRenderer;

    public static final String SHADER_SIMPLE_FONT = "integrated/simple_font";

    public CVEngine(String name) {
        this.name = name;
    }

    private void init() {

        GLFW.glfwSetErrorCallback((code, desc) -> {
            System.out.println("[GLFW ERROR] " + code + GLFWErrorCallback.getDescription(desc));
        });

        if (! GLFW.glfwInit()) {
            throw new IllegalStateException("Could not init GLFW");
        }

        //Set render hints
        GLFW.glfwDefaultWindowHints();
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_FORWARD_COMPAT, GLFW.GLFW_TRUE);

        this.windowHandle = GLFW.glfwCreateWindow(800, 600, "CV Engine: " + name, 0, 0);

        if (this.windowHandle == 0) {
            throw new RuntimeException("Filed to create a window and setup OpenGL");
        }

        //Setup window
        GLFW.glfwMakeContextCurrent(this.windowHandle);
        GLFW.glfwSwapInterval(1);
        GL.createCapabilities();
        this.lwjglErrorCallback = GLUtil.setupDebugMessageCallback();
        GL11.glClearColor(0.02f, 0.02f, 0.02f, 1.0f);

        initialiseCoreServices();
        initialiseIntegratedResources();

        setupImGui();
    }


    private void initialiseCoreServices() {
        this.inputManager = new GLFWInputManager(windowHandle);
        this.batcher = new Batch2D();
        this.resourceManager = new ResourceManager();
        this.levelRenderer = new LevelRenderer(batcher, resourceManager);
    }


    private void initialiseIntegratedResources() {
        this.resourceManager.loadShadersFromDisc(SHADER_SIMPLE_FONT, new File("data/integrated/shaders/simpleTexture.vert"), new File("data/integrated/shaders/simpleTexture.frag"));
    }


    private void setupImGui() {
        ImGui.createContext();
        ImGui.styleColorsDark();
        glfwImGui = new ImGuiImplGlfw();
        openGlImGui = new ImGuiImplGl3();

        ImGuiIO io = ImGui.getIO();
        ImFont font = io.getFonts().addFontDefault();
//        ImFont experience = io.getFonts().addFontFromFileTTF("data/fonts/ubuntu.ttf", 32);
//        io.setFontDefault(experience);
        io.addConfigFlags(ImGuiConfigFlags.ViewportsEnable);
        io.addConfigFlags(ImGuiConfigFlags.DockingEnable);
        io.setConfigViewportsNoDecoration(false);
        io.setConfigViewportsNoTaskBarIcon(false);
        io.setIniFilename(null);


        glfwImGui.init(this.windowHandle, true);
        openGlImGui.init();
    }

    public void run(IApplication application) {
        init();
        application.init(this);

        while (! GLFW.glfwWindowShouldClose(this.windowHandle)) {


            if (inputManager.keyClick(RawInput.KEY_Q) && inputManager.keyClick(RawInput.LCTRL)) {
                shutdown();
            }

            glfwImGui.newFrame();
            openGlImGui.newFrame();
            ImGui.newFrame();

            //Always run the main application first
            application.loop();

            //Then check if the current state needs running
            if (currentState != null) {
                currentState.update(inputManager);
                currentState.render();
            }


            ImGui.render();
            openGlImGui.renderDrawData(ImGui.getDrawData());


            //ImGui flags
            if (ImGui.getIO().hasConfigFlags(ImGuiConfigFlags.ViewportsEnable)) {
                final long toRestore = GLFW.glfwGetCurrentContext();
                ImGui.updatePlatformWindows();
                ImGui.renderPlatformWindowsDefault();
                GLFW.glfwMakeContextCurrent(toRestore);
            }


            GLFW.glfwSwapBuffers(this.windowHandle);
            GLFW.glfwPollEvents();
        }
        application.cleanup();
    }
    
    
    public Vector2i windowMetrics() {
        int[] width = new int[1];
        int[] height = new int[1];
        GLFW.glfwGetWindowSize(this.windowHandle, width, height);
        return new Vector2i(width[0], height[0]);
    }


    @Override
    public void setCurrentApplicationState(IApplicationState state) {
        state.init();
        if (this.currentState != null) {
            this.currentState.cleanup();
        }
        this.currentState = state;
    }

    private void throwFile() {
        throw new RuntimeException("Error when opening file");
    }


    public void shutdown() {
        GLFW.glfwSetWindowShouldClose(this.windowHandle, true);
    }

    @Override
    public InputManager inputs() {
        return inputManager;
    }

    @Override
    public Batch2D renderer() {
        return this.batcher;
    }

    @Override
    public LevelRenderer levelRenderer() {
        return levelRenderer;
    }

    @Override
    public IResourceManager resourceManager() {
        return this.resourceManager;
    }

    @Override
    public void close() {

        batcher.close();
        resourceManager.destroy();

        if (this.lwjglErrorCallback != null) {
            this.lwjglErrorCallback.close();
        }

        openGlImGui.shutdown();
        //todo this causes null pointer exception and I don't know why
        //According to the documentation I have done everything correctly ?
//        glfwImGui.shutdown();
        ImGui.destroyContext();

        GLFW.glfwSetErrorCallback(null).free();

        glfwFreeCallbacks(this.windowHandle);
        GLFW.glfwDestroyWindow(this.windowHandle);
        GLFW.glfwTerminate();

    }

}
