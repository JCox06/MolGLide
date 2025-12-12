package uk.co.jcox.chemvis.application.ui

import imgui.ImGui
import imgui.ImVec2
import imgui.flag.ImGuiStyleVar
import imgui.flag.ImGuiWindowFlags

object ImGuiUtils {

    //Adapted from https://github.com/ocornut/imgui/issues/3518
    fun populateStatus(content: () -> Unit) {
        val viewport = ImGui.getMainViewport()

        ImGui.setNextWindowPos(viewport.posX, viewport.posY + viewport.sizeY - ImGui.getFrameHeight())
        ImGui.setNextWindowSize(ImVec2(viewport.sizeX, ImGui.getFrameHeight()))

        val flags = ImGuiWindowFlags.NoDecoration or
                    ImGuiWindowFlags.NoMove or ImGuiWindowFlags.NoScrollWithMouse or
                    ImGuiWindowFlags.NoSavedSettings or
                    ImGuiWindowFlags.MenuBar or ImGuiWindowFlags.NoBackground

        ImGui.pushStyleVar(ImGuiStyleVar.WindowRounding, 0.0f)
        ImGui.pushStyleVar(ImGuiStyleVar.WindowPadding, ImVec2(0.0f, 0.0f))
        ImGui.pushStyleVar(ImGuiStyleVar.WindowBorderSize, 0.0f)


        if (ImGui.begin("StatusBar", flags)) {


            if (ImGui.beginMenuBar()) {
                content()
                ImGui.endMenuBar()
            }
            ImGui.end()
        }

        ImGui.popStyleVar(3)
    }
}