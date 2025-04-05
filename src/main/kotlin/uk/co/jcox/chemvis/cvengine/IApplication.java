package uk.co.jcox.chemvis.cvengine;

public interface IApplication {

    void init(CVEngine engine);
    void loop(CVEngine engine);
    void cleanup();

}
