package uk.co.jcox.chemvis.application.ui

import imgui.ImGui
import imgui.ImVec2
import imgui.ImVec4
import imgui.flag.ImGuiCond
import imgui.flag.ImGuiStyleVar
import imgui.type.ImString
import uk.co.jcox.chemvis.application.MolGLide
import uk.co.jcox.chemvis.cvengine.ICVServices

class WelcomeUI (
    private val services: ICVServices,
) {

    var newWindow: () -> Unit = {}

    private val scale = FloatArray(1)

    val msaaSamples = IntArray(1)

    fun setup() {
        scale[0] = ImGui.getIO().fontGlobalScale
        msaaSamples[0] = 4.coerceIn(1, services.getPlatformMSAAMaxSamples())
    }

    fun draw(dockingID: Int) {
        ImGui.setNextWindowDockID(dockingID, ImGuiCond.Always)
        ImGui.begin("WelcomeUI")

        ImGui.textColored(ImVec4(1.0f, 0.0f, 1.0f, 1.0f),"MolGLide ${MolGLide.VERSION}")
        ImGui.text(MolGLide.WEBSITE)
        ImGui.text("Open Source Molecular Drawing System")


        ImGui.separator()
        ImGui.bulletText("Use the Atom Bond Tool to create molecules and add atoms")
        ImGui.bulletText("Use the Implicit Group Move Tool to move the position of implicit hydrogens")
        ImGui.bulletText("Press and hold the keys [1] and [2] on your keyboard to apply wedged and dashed bonds for stereochemistry")
        ImGui.bulletText("[COMING SOON] Use the template tool to quickly add common structures and rings")

        ImGui.separator()

        if (ImGui.button("New Project")) {
            newWindow()
        }

        ImGui.separatorText("Settings")

        if (ImGui.sliderFloat("UI Scale", scale, 0.0f, 10.0f)) {
            ImGui.getIO().fontGlobalScale = scale[0]
        }

        ImGui.sliderInt("MSAA Samples", msaaSamples, 1, services.getPlatformMSAAMaxSamples())

        ImGui.end()
    }
}