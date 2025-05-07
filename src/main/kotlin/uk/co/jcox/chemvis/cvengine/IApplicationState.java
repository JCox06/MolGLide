package uk.co.jcox.chemvis.cvengine;

public interface IApplicationState {
    void init();
    void update();
    void render();
    void cleanup();
}
