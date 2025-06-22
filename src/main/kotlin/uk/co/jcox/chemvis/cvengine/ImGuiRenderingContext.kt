package uk.co.jcox.chemvis.cvengine

import imgui.ImGui

class ImGuiRenderingContext : IRenderTargetContext{

    var iwidth = 1.0f
    var iheight = 1.0f

    override fun recalculate() {
        this.iwidth = ImGui.getWindowWidth()
        this.iheight = ImGui.getWindowHeight()
    }

    override fun getHeight() : Float {
        return iheight
    }

    override fun getWidth() : Float{
        return iwidth
    }

}