package uk.co.jcox.chemvis.cvengine

import org.joml.Vector2f

/**
 * This class is passed to each App State
 * Different Render targets are bound to different windows/textures/things
 * By using this interface, you can make each app state aware of its Render Target
 * For instance, stuff rendered to ImGui using a texture is given an ImGuiWindowRenderingContext
 *
 */
interface IRenderTargetContext {

    fun getHeight() : Float
    fun getWidth() : Float

    fun recalculate()

    fun getMousePos(inputManager: InputManager) : Vector2f
    fun setRelativeWindowPos(windowPos: Vector2f)
}