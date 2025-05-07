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

        this.inputManager = new GLFWInputManager(windowHandle);

        setupImGui();
    }

    private void setupImGui() {
        ImGui.createContext();
        ImGui.styleColorsDark();
        glfwImGui = new ImGuiImplGlfw();
        openGlImGui = new ImGuiImplGl3();

        ImGuiIO io = ImGui.getIO();
        ImFont font = io.getFonts().addFontDefault();
        ImFont experience = io.getFonts().addFontFromFileTTF("data/fonts/ubuntu.ttf", 32);
        io.setFontDefault(experience);
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
    public String loadShaderSourceResource(File file) {
        try {
            return Files.readString(file.toPath());
        } catch (IOException e) {
           throwFile();
        }
       return "";
    }

    @Override
    public void setCurrentApplicationState(IApplicationState state) {
        state.init();
        if (this.currentState != null) {
            this.currentState.cleanup();
        }
        this.currentState = state;
    }



    @Override
    public int loadTextureResource(File file) {
        if (! file.isFile()) {
            throwFile();
        }

        String stringLoc = file.getAbsolutePath().toString();

        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer width = stack.mallocInt(1);
            IntBuffer height = stack.mallocInt(1);
            IntBuffer nrChannels = stack.mallocInt(1);

            STBImage.stbi_set_flip_vertically_on_load(true);
            ByteBuffer dataBuff = STBImage.stbi_load(stringLoc, width, height, nrChannels, 4);

            if (dataBuff == null) {
                throw new RuntimeException();
            }

            return loadOpenGlTexture(dataBuff, width.get(), height.get(), true);

        }
    }

    private int loadOpenGlTexture(ByteBuffer dataBuffer, int width, int height, boolean stbLoaded) {
        return loadOpenGlTexture(dataBuffer, width, height, stbLoaded, GL11.GL_LINEAR_MIPMAP_LINEAR, GL11.GL_LINEAR);
    }

    private int loadOpenGlTexture(ByteBuffer dataBuffer, int width, int height, boolean stbLoaded, int minFilter, int magFilter) {
        int glTexture = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, glTexture);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width, height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, dataBuffer);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, minFilter);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, magFilter);
        GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);

        if (stbLoaded) {
            STBImage.stbi_image_free(dataBuffer);
        }
        //If the texture was loaded elsewhere (Direct ByteBuffer Allocation from JVM)
        //Then the JVM will automatically free the native heap memory once the GC runs
        //Do not free random direct bytebuffers as it causes heap corruption

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        return glTexture;
    }


    @Override
    public BitmapFont loadFontResource(File file, int size, String glyphs, boolean debugImage, TextureManager textureManager) {
        if (! file.isFile()) {
            throwFile();
        }

        //Get the font metrics so we can get glyph size, etc
        Font font = new Font(file.getPath(), Font.PLAIN, size);
        FontMetrics fontMetrics = getFontMetrics(font);

        //Each char has a different width, but same height
        //To avoid a runtime error, assume they are all the largest size
        int maxCharWidth = 0; //need to calculate
        int charHeight = (fontMetrics.getHeight()); //They have the same height

        for (char c: glyphs.toCharArray()) {
            maxCharWidth = Math.max(maxCharWidth, fontMetrics.charWidth(c));
        }


        //Now create texture dimensions
        int textureUnit = (int) Math.sqrt(glyphs.length()) + 1;
        int proposedWidth = textureUnit * maxCharWidth;

        int charHeightPadding = charHeight + size;

        int proposedHeight = textureUnit * charHeight;
        int squareTextureSize = Math.max(proposedHeight, proposedWidth);

        //Create the actual image
        final Map<Character, BitmapFont.GlyphData> fontAtlas = new HashMap<>();
        BufferedImage atlasImage = new BufferedImage(squareTextureSize, squareTextureSize, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = atlasImage.createGraphics();
        g2d.setFont(font);
        g2d.setColor(Color.WHITE);

        int glyphXPlacement = 0;
        int glyphYPlacement = charHeight;

        for (char c: glyphs.toCharArray()) {

            if (glyphXPlacement + maxCharWidth >= squareTextureSize) {
                glyphXPlacement = 0;
                glyphYPlacement += charHeight;
            }

            g2d.drawString(String.valueOf(c), glyphXPlacement, glyphYPlacement);

            BitmapFont.GlyphData glyphData = new BitmapFont.GlyphData(
                    (float) fontMetrics.charWidth(c),
                    (float) charHeight,
                    (float) glyphXPlacement / squareTextureSize,
                    (float) (glyphYPlacement + (0.30f * size)) / squareTextureSize,
                    (float) fontMetrics.charWidth(c) / squareTextureSize,
                    (float) charHeight / squareTextureSize
            );


            fontAtlas.put(c, glyphData);

            glyphXPlacement += maxCharWidth;
        }

        //Now save image if debug is enabled
        if (debugImage) {
            saveBitmapFont(atlasImage);
        }

        //Now load this as an image into OpenGL
        ByteBuffer textureData = convertImageData(atlasImage);
        int glTextureObject = loadOpenGlTexture(textureData, squareTextureSize, squareTextureSize, false, GL11.GL_LINEAR, GL11.GL_LINEAR);

        String textureID = file.getPath();
        textureManager.manageTexture(textureID, glTextureObject);

        BitmapFont bitmapFont = new BitmapFont(size, textureManager, textureID, fontAtlas);

        g2d.dispose();

        return bitmapFont;
    }


    private ByteBuffer convertImageData(BufferedImage image) {
        int[] pixelDataInt = image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth());
        ByteBuffer buffer = ByteBuffer.allocateDirect(image.getWidth() * image.getHeight() * 4);

        for (int y = image.getHeight() - 1; y >= 0; y--) {
            for (int x = 0; x < image.getWidth(); x++) {
                int pixel = pixelDataInt[image.getWidth() * y + x];
                buffer.put((byte) ((pixel >> 16) & 0xFF));
                buffer.put((byte) ((pixel >> 8) & 0xFF));
                buffer.put((byte) (pixel & 0xFF));
                buffer.put((byte) ((pixel >> 24) & 0xFF));
            }
        }

        buffer.flip();

        return buffer;
    }

    private void saveBitmapFont(BufferedImage bufferedImage) {
        File outputFile = new File("data/temp/font-generated.png");
        try {
            ImageIO.write(bufferedImage, "png", outputFile);
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }

    private FontMetrics getFontMetrics(Font font) {
        BufferedImage tempImage = new BufferedImage(1, 1, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics g2d = tempImage.createGraphics();
        g2d.setFont(font);
        g2d.setColor(Color.WHITE);
        FontMetrics metrics = g2d.getFontMetrics();
        g2d.dispose();
        return metrics;
    }

    private void throwFile() {
        throw new RuntimeException("Error when opening file");
    }


    public void shutdown() {
        GLFW.glfwSetWindowShouldClose(this.windowHandle, true);
    }

    @Override
    public InputManager getInputManager() {
        return inputManager;
    }

    @Override
    public void close() {
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
