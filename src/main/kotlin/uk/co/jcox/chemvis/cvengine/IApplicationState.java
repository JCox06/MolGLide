package uk.co.jcox.chemvis.cvengine;

public interface IApplicationState {
    void init();
    void update(InputManager inputManager, float timeElapsed);
    void render();
    void cleanup();
}
