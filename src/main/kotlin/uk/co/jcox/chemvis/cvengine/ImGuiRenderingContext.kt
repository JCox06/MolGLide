package uk.co.jcox.chemvis.cvengine

import imgui.ImGui
import org.joml.Vector2f
import org.lwjgl.system.Platform

class ImGuiRenderingContext : IRenderTargetContext{

    var imGuiWidth = 1.0f
    var imGuiHeight = 1.0f

    var imGuiPos = Vector2f()

    override fun recalculate() {
        this.imGuiWidth = ImGui.getWindowWidth()
        this.imGuiHeight = ImGui.getWindowHeight()
    }

    override fun getHeight() : Float {
        return imGuiHeight
    }

    override fun getWidth() : Float{
        return imGuiWidth
    }


    override fun setRelativeWindowPos(windowPos: Vector2f) {
        this.imGuiPos = windowPos
    }


    override fun getMousePos(inputManager: InputManager): Vector2f {


        val newVec = Vector2f(
            ImGui.getMousePosX() - imGuiPos.x,
            ImGui.getMousePosY() - imGuiPos.y
        )
        return newVec
    }
}