package uk.co.jcox.chemvis.application.ui

import imgui.ImGui
import imgui.flag.ImGuiStyleVar
import org.lwjgl.Version.getVersion
import org.lwjgl.glfw.GLFW
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GLUtil
import org.lwjgl.system.Platform
import org.openscience.cdk.CDK
import uk.co.jcox.chemvis.application.MolGLide
import java.awt.Desktop
import java.net.URI
import javax.swing.FocusManager

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
            showVersionTable()
        }
    }


    private fun showVersionTable() {
        ImGui.begin("MolGLide About")

        ImGui.text("MolGLide 2D molecular editor")

        ImGui.separator()

        if (ImGui.beginTable("Version Table", 2)) {

            versionIndex("MolGLide", MolGLide.VERSION)
            versionIndex("OpenGL", GL11.glGetString(GL11.GL_VERSION))
            versionIndex("LWJGL", getVersion())
            versionIndex("GLFW", GLFW.glfwGetVersionString())
            versionIndex("Dear ImGui", ImGui.getVersion())
            versionIndex("CDK", CDK.getVersion())
            versionIndex("JRE", Runtime.version().toString())
            versionIndex("Platform", "${Platform.get()} ${Platform.getArchitecture()}")
            ImGui.endTable()
        }

        ImGui.separator()

        if (ImGui.button("Visit Website")) {
            Desktop.getDesktop().browse(URI(MolGLide.WEBSITE))
        }

        ImGui.sameLine()

        if (ImGui.button("Close Window")) {
            showAbout = false
        }

        ImGui.end()

    }


    private fun versionIndex(component: String, version: String?) {
        ImGui.tableNextRow()
        ImGui.tableNextColumn()
        ImGui.text("$component version")
        ImGui.tableNextColumn()
        ImGui.text(version)
    }
}