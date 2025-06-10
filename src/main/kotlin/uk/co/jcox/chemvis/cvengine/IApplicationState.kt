package uk.co.jcox.chemvis.cvengine

import org.joml.Vector2f

interface IApplicationState {

    fun init()
    fun update(inputManager: InputManager, timeElapsed: Float)
    fun render(viewport: Vector2f)
    fun cleanup()

}