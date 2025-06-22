package uk.co.jcox.chemvis.cvengine

import org.joml.Vector2f

abstract class ApplicationState (
    val renderTargetContext: IRenderTargetContext
) {

    var paused = false
    private set

    abstract fun init()
    abstract fun update(inputManager: InputManager, timeElapsed: Float)
    abstract fun render(viewport: Vector2f)
    abstract fun cleanup()

    fun pause() {
        if (! paused) {
            onPause()
        }
        paused = true
    }


    fun resume() {
        if (paused) {
            onResume()
        }

        paused = false
    }


    protected abstract fun onPause()

    protected abstract fun onResume()
}