package uk.co.jcox.chemvis;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.opengl.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;

//todo - Maybe at some point, all OpenGL stuff should be removed from this file!

public class BitmapFont {

    public static final String CHARACTERS = "@#!\"£$%^&*()[]:;/><,.|\\ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz123456789 ";

    private final Map<Character, Glyph> glyphAtlas;
    private final int textureObject;
    private final int fontCanvasVertexArray;
    private final String drawable;
    private final int fontSize;


    public BitmapFont(int fontSize, Map<Character, Glyph> glyphAtlas, int textureObject, String drawable) {
        this.fontSize = fontSize;
        this.glyphAtlas = glyphAtlas;
        this.textureObject = textureObject;
        this.drawable = drawable;

        Glyph recovery = this.glyphAtlas.get(drawable.charAt(0));

        float vertices[] = {
                1.0f * fontSize,  1.0f * fontSize, 0.0f * fontSize, recovery.xTexA, recovery.yTexA,  // top right
                1.0f * fontSize, -1.0f * fontSize, 0.0f * fontSize, + recovery.xTexA, 0.0f,  // bottom right
                -1.0f * fontSize, -1.0f * fontSize, 0.0f * fontSize, 0.0f, 0.0f,  // bottom left
                -1.0f * fontSize,  1.0f * fontSize, 0.0f * fontSize, 0.0f, recovery.yTexA  // top left
        };
        int indices[] = {  // note that we start from 0!
                0, 1, 3,   // first triangle
                1, 2, 3    // second triangle
        };

        //TESTING - Create some basic geometry
        int vertexArray = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(vertexArray);
        int vertexBuff = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vertexBuff);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertices, GL15.GL_STATIC_DRAW);
        int stride = 5 * Float.BYTES;
        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, stride, 0L);
        GL20.glEnableVertexAttribArray(0);
        GL33.glVertexAttribDivisor(3, 1);

        GL20.glVertexAttribPointer(1, 2, GL11.GL_FLOAT, false, stride, 3 * Float.BYTES);
        GL20.glEnableVertexAttribArray(1);
        GL33.glVertexAttribDivisor(2, 1);

        int indexBuff = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, indexBuff);
        GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indices, GL15.GL_STATIC_DRAW);

        this.fontCanvasVertexArray = vertexArray;
    }

    public void text(ShaderProgram program, String label, float renderX, float renderY) {

        GL33.glBindVertexArray(this.fontCanvasVertexArray);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.textureObject);
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        for (char c: label.toCharArray()) {
            if (! drawable.contains(String.valueOf(c))) {
                c = drawable.charAt(0);
            }

            //Set position and draw
            Glyph glyph = this.glyphAtlas.get(c);
            program.uniform("model", new Matrix4f().translate(renderX, renderY, 0.0f));
            program.uniform("uTextureOffset", new Vector2f(glyph.xTex, -glyph.yTex));
            GL11.glDrawElements(GL11.GL_TRIANGLES, 6, GL11.GL_UNSIGNED_INT, 0);
            renderX = renderX + fontSize;
        }


        GL30.glBindVertexArray(0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
    }


    public static class Glyph {
        public final int x;
        public final int y;
        public final int width;
        public final int height;
        public final float xTex;
        public final float yTex;
        public final float xTexA;
        public final float yTexA;
        public final float xAbs;

        public Glyph(int x, int y, int width, int height, float xTex, float yTex, float xTexA, float yTexA, float xAbs) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.xTex = xTex;
            this.yTex = yTex;
            this.xTexA = xTexA;
            this.yTexA = yTexA;
            this.xAbs = xAbs;
        }
    }


    public static BitmapFont generate(File trueTypeFile, int size, boolean saveImage) {
        if (! trueTypeFile.isFile()) {
            throw new RuntimeException();
        }

        Font font = new Font(trueTypeFile.getPath(), Font.PLAIN, size);

        //Step 1 - Create a temp "fake" image so we can get a rendering context
        BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics g2d = img.createGraphics();
        g2d.setFont(font);
        g2d.setColor(Color.white);
        FontMetrics fontMetrics = g2d.getFontMetrics();
        g2d.dispose();


        //Step 2 - Because char width varies, get the max possible size and just assume true for all
        int maxCharWidth = 0;
        int charHeight = fontMetrics.getHeight();

        int numberOfGlyphs = CHARACTERS.length();
        int textureUnitSquare = ( int ) Math.sqrt(numberOfGlyphs) + 1;

        for (char c : CHARACTERS.toCharArray()) {
            maxCharWidth = Math.max(maxCharWidth, fontMetrics.charWidth(c));
        }

        //Step 3 - Calculate texture dimensions (Ensure square texture)
        int texutreSquareUnit = (int) Math.sqrt(CHARACTERS.length()) + 1;
        int proposedTextureWidth = maxCharWidth * textureUnitSquare;
        int proposedTextureHeight = charHeight * textureUnitSquare;

        int sdim = Math.max(proposedTextureHeight, proposedTextureWidth);
        int cdim = nextPower(sdim);

        //Step 4 - Create the actual image

        final Map<Character, Glyph> glypthAtlas = new HashMap<>();

        img = new BufferedImage(cdim, cdim, BufferedImage.TYPE_INT_ARGB);
        g2d = img.createGraphics();
        g2d.setFont(font);
        g2d.setColor(Color.BLACK);

        int xpos = 0;
        int ypos = charHeight;

        for (char c: CHARACTERS.toCharArray()) {
            g2d.drawString(String.valueOf(c), xpos, ypos);


            Glyph glyph = new Glyph(
                    xpos,
                    ypos,
                    fontMetrics.charWidth(c),
                    charHeight,
                    (float) xpos / cdim,
                    (float) ypos/ cdim,
                    (float) fontMetrics.charWidth(c) / cdim,
                    (float) charHeight / cdim,
                    (float) maxCharWidth / cdim
//                    (float) fontMetrics.charWidth(c) / cdim
            );


            glypthAtlas.put(c, glyph);

            xpos += maxCharWidth;

            if (xpos >= proposedTextureWidth) {
                xpos = 0;
                ypos += charHeight;
            }
        }


        if (saveImage) {
            saveBitmapFont(img);
        }

        //Final step  - Load into OpenGL and send data to the GPU
        int glTextureObject = loadIntoOpenGL(img);

        g2d.dispose();



        return new BitmapFont(font.getSize(), glypthAtlas, glTextureObject, CHARACTERS);

    }


    private static int loadIntoOpenGL(BufferedImage img) {
        //Step 1 - Convert to correct format
        int[] pixelData = img.getRGB(0, 0, img.getWidth(), img.getHeight(), null, 0, img.getWidth());
        ByteBuffer buffer = ByteBuffer.allocateDirect(img.getWidth() * img.getHeight() * 4);

        for (int y = img.getHeight() -1 ; y >= 0; y--) {
            for (int x = 0; x < img.getWidth(); x++) {
                int pixel = pixelData[img.getWidth() * y + x];
                buffer.put((byte) ((pixel >> 16) & 0xFF));
                buffer.put((byte) ((pixel >> 8) & 0xFF));
                buffer.put((byte) (pixel & 0xFF));
                buffer.put((byte) ((pixel >> 24) & 0xFF));
            }
        }

        //Put the buffer in the correct format
        //BufferedImage and OpenGL use different mappings for the texture
        buffer.flip();

        //Step 2 - Create OpenGL texture
        int textureObject = GL11.glGenTextures();
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureObject);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST_MIPMAP_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_REPEAT);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_REPEAT);

        //Load texture to OpenGL
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, img.getWidth(), img.getHeight(), 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);
        GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);

        return textureObject;
    }

    private static void saveBitmapFont(BufferedImage img) {
        File outputFile = new File("data/temp/font-generated.png");
        System.out.println("Saving font atlas image data to: " + outputFile.getAbsolutePath());
        try {
            ImageIO.write(img, "png", outputFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    //Sending a texture to the GPU that is a power of 2 is more efficient
    private static int nextPower(int value) {
        if (log2(value) % 1.0 > 0) {
            //Not power of 2
            return nextPower(++value);
        }
        return value;
    }

    private static double log2(int number) {
        return  Math.log(number) / Math.log(2);
    }
}
