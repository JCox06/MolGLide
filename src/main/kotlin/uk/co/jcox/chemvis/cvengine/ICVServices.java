package uk.co.jcox.chemvis.cvengine;

import org.joml.Vector2i;

public interface ICVServices {


    Vector2i windowMetrics();

    void setCurrentApplicationState(IApplicationState state);

    InputManager inputs();

    Batch2D renderer();

    LevelRenderer levelRenderer();

    IResourceManager resourceManager();

    void toggleDebugPanel();

    void setViewport(int a, int b, int c, int d);
}