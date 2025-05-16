package uk.co.jcox.chemvis.cvengine;

import org.joml.Vector2i;

import java.io.File;

public interface ICVServices {


    Vector2i windowMetrics();

    BitmapFont loadFontResource(File file, int size, String glyphs, boolean debugImage);

    int loadTextureResource(File file);

    void setCurrentApplicationState(IApplicationState state);

    InputManager inputs();

    Batch2D renderer();

    TextureManager textures();

    ShaderProgramManager programs();
}