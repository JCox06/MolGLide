package uk.co.jcox.chemvis.cvengine;

public interface IEngineInput {

    default void keyClickEvent(int key, int action, int mods) {

    }

    default void mouseClickEvent(int button, int action, int mods) {

    }

    default void mouseMoveEvent(double xpos, double ypos) {

    }
}
