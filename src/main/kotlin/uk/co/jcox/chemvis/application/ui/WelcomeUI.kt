package uk.co.jcox.chemvis.application.ui

import imgui.ImGui
import imgui.ImVec2
import imgui.ImVec4
import imgui.flag.ImGuiCond
import imgui.flag.ImGuiStyleVar
import uk.co.jcox.chemvis.application.MolGLide

class WelcomeUI {

    var newWindow: () -> Unit = {}

    private val scale = FloatArray(1)

    val msaaSamples = IntArray(1)

    fun setup() {
        scale[0] = ImGui.getIO().fontGlobalScale
        msaaSamples[0] = 1
    }

    fun draw(dockingID: Int) {
        ImGui.setNextWindowDockID(dockingID, ImGuiCond.Always)
        ImGui.begin("WelcomeUI")

        ImGui.textColored(ImVec4(1.0f, 0.0f, 1.0f, 1.0f),"MolGLide ${MolGLide.VERSION}")
        ImGui.text(MolGLide.WEBSITE)
        ImGui.text("Open Source Molecular Drawing System")

        ImGui.separator()

        if (ImGui.button("New Project")) {
            newWindow()
        }

        ImGui.separatorText("Settings")

        if (ImGui.sliderFloat("UI Scale", scale, 0.0f, 10.0f)) {
            ImGui.getIO().fontGlobalScale = scale[0]
        }

        ImGui.sliderInt("MSAA Samples", msaaSamples, 1, 16)

        ImGui.end()
    }
}