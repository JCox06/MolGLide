package uk.co.jcox.chemvis.application.ui

import imgui.ImGui
import imgui.flag.ImGuiStyleVar
import org.lwjgl.Version.getVersion
import uk.co.jcox.chemvis.application.MolGLide

class WelcomeUI {

    private var showImGuiMetrics = false
    private var showImGuiStyleEditor = false
    private var showAbout = false

    fun draw(dockID: Int) {

        ImGui.setNextWindowDockID(dockID)

        ImGui.begin("Welcome")

        ImGui.textWrapped("Welcome to MolGLide. Click File -> New to open a new project")

        ImGui.separatorText("Debug Options")

        if (ImGui.checkbox("Show Metrics", showImGuiMetrics)) {
            showImGuiMetrics = !showImGuiMetrics
        }

        if (ImGui.checkbox("Show Style Editor", showImGuiStyleEditor)) {
            showImGuiStyleEditor = !showImGuiStyleEditor
        }

        if (ImGui.checkbox("Show About", showAbout)) {
            showAbout = !showAbout
        }

        ImGui.end()

        drawWidgets()
    }


    private fun drawWidgets() {

        if (showImGuiMetrics) {
            ImGui.showMetricsWindow()
        }

        if (showImGuiStyleEditor) {
            ImGui.showStyleEditor()
        }

        if (showAbout) {
            ImGui.showAboutWindow()

            ImGui.begin("MolGLide About")

            ImGui.text("MolGLide 2D molecular editor")

            ImGui.text("MolGLide version ${MolGLide.VERSION}")
            ImGui.text("LWJGL Version ${getVersion()}")

            ImGui.end()
        }
    }
}