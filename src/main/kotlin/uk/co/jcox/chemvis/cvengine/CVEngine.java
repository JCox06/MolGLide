package uk.co.jcox.chemvis.cvengine;

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

public class CVEngine implements AutoCloseable{
    private final String name;
    private Callback lwjglErrorCallback;
    private long windowHandle;

    private IEngineInput inputHandler;


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

        this.windowHandle = GLFW.glfwCreateWindow(800, 600, "CV Engine: " + name + " render", 0, 0);

        if (this.windowHandle == 0) {
            throw new RuntimeException("Filed to create a window and setup OpenGL");
        }

        //Setup window
        GLFW.glfwMakeContextCurrent(this.windowHandle);
        GLFW.glfwSwapInterval(1);
        GL.createCapabilities();
        this.lwjglErrorCallback = GLUtil.setupDebugMessageCallback();
        GL11.glClearColor(0.02f, 0.02f, 0.02f, 1.0f);

        setupInputCallbacks();
    }

    private void setupInputCallbacks() {
        GLFW.glfwSetKeyCallback(this.windowHandle, (win, key, scancode, action, mods) -> {
           this.inputHandler.keyClickEvent(key, action, mods);
        });

        GLFW.glfwSetMouseButtonCallback(this.windowHandle, (win, button, action, mods) -> {
            this.inputHandler.mouseClickEvent(button, action, mods);
        });

        GLFW.glfwSetCursorPosCallback(this.windowHandle, (win, xpos, ypos) -> {
            this.inputHandler.mouseMoveEvent(xpos, ypos);
        });
    }

    public void run(IApplication application) {
        init();
        this.inputHandler = new IEngineInput() {};
        application.init(this);

        while (! GLFW.glfwWindowShouldClose(this.windowHandle)) {
            application.loop(this);

            GLFW.glfwSwapBuffers(this.windowHandle);
            GLFW.glfwPollEvents();
        }
        application.cleanup();
    }

    public int windowX() {
        int[] width = new int[1];
        int[] height = new int[1];
        GLFW.glfwGetWindowSize(this.windowHandle, width, height);
        return width[0];
    }

    public int windowY() {
        int[] width = new int[1];
        int[] height = new int[1];
        GLFW.glfwGetWindowSize(this.windowHandle, width, height);
        return height[0];
    }


    public String loadShaderSourceResource(File file) {
        try {
            return Files.readString(file.toPath());
        } catch (IOException e) {
           throwFile();
        }
       return "";
    }

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

            return loadOpenGlTexture(dataBuff, width.get(), height.get());

        }
    }

    private int loadOpenGlTexture(ByteBuffer dataBuffer, int width, int height) {
        int glTexture = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, glTexture);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width, height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, dataBuffer);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR_MIPMAP_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);
        STBImage.stbi_image_free(dataBuffer);

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        return glTexture;
    }


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
        int charHeight = fontMetrics.getHeight(); //They have the same height

        for (char c: glyphs.toCharArray()) {
            maxCharWidth = Math.max(maxCharWidth, fontMetrics.charWidth(c));
        }


        //Now create texture dimensions
        int textureUnit = (int) Math.sqrt(glyphs.length()) + 1;
        int proposedWidth = textureUnit * maxCharWidth;
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

            System.out.println("Current Char: " + c);

            g2d.drawString(String.valueOf(c), glyphXPlacement, glyphYPlacement);

            BitmapFont.GlyphData glyphData = new BitmapFont.GlyphData(
                    (float) glyphXPlacement / squareTextureSize,
                    (float) glyphYPlacement / squareTextureSize,
                    (float) maxCharWidth / squareTextureSize,
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
        int glTextureObject = loadOpenGlTexture(textureData, squareTextureSize, squareTextureSize);

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

    public void setActiveInputHandler(IEngineInput engineInput) {
        this.inputHandler = engineInput;
    }

    @Override
    public void close() throws Exception {
        if (this.lwjglErrorCallback != null) {
            this.lwjglErrorCallback.close();
        }

        GLFW.glfwSetErrorCallback(null).free();

        glfwFreeCallbacks(this.windowHandle);
        GLFW.glfwDestroyWindow(this.windowHandle);
        GLFW.glfwTerminate();
    }
}
