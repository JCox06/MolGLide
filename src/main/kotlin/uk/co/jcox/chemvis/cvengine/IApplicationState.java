package uk.co.jcox.chemvis.cvengine;

import org.joml.Vector2f;

public interface IApplicationState {
    void init();
    void update(InputManager inputManager, float timeElapsed);
    void render(Vector2f viewport);
    void cleanup();
}
