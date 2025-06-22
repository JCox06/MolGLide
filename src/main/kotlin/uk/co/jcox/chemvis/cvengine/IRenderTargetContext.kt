package uk.co.jcox.chemvis.cvengine

import org.joml.Vector2f

interface IRenderTargetContext {

    fun getHeight() : Float
    fun getWidth() : Float

    fun recalculate()

    fun getMousePos(inputManager: InputManager) : Vector2f
    fun setImGuiWinMetrics(windowPos: Vector2f)
}