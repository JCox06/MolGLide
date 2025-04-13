package uk.co.jcox.chemvis.cvengine;

public interface IApplication {

    void init(ICVServices engineServices);
    void loop();
    void cleanup();

}
