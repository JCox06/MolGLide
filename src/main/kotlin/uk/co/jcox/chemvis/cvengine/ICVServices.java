package uk.co.jcox.chemvis.cvengine;

import org.joml.Vector2i;

import java.io.File;

public interface ICVServices {


    Vector2i windowMetrics();

    BitmapFont loadFontResource(File file, int size, String glyphs, boolean debugImage, TextureManager textureManager);

    int loadTextureResource(File file);

    String loadShaderSourceResource(File file);

    void setCurrentApplicationState(IApplicationState state);

    InputManager getInputManager();
}