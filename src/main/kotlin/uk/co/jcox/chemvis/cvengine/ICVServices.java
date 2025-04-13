package uk.co.jcox.chemvis.cvengine;

import org.joml.Vector2i;

import java.io.File;

public interface ICVServices {

    void setActiveInputHandler(IEngineInput engineInput);

    Vector2i windowMetrics();

    BitmapFont loadFontResource(File file, int size, String glyphs, boolean debugImage, TextureManager textureManager);

    int loadTextureResource(File file);

    String loadShaderSourceResource(File file);

    long glfwEngineWindow();
}