package uk.co.jcox.chemvis.application.ui

import imgui.ImGui
import imgui.flag.ImGuiStyleVar
import org.lwjgl.Version.getVersion
import org.lwjgl.glfw.GLFW
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL30
import org.lwjgl.opengl.GL32
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
    private var extendedOpenGL = false

    private var MSAACount = intArrayOf(4)
    private var globalScale = floatArrayOf(1.0f)

    val highestSamples = GL11.glGetInteger(GL32.GL_MAX_DEPTH_TEXTURE_SAMPLES)

    fun draw(dockID: Int) {

        ImGui.setNextWindowDockID(dockID)

        ImGui.begin("Welcome")

        ImGui.textWrapped("Welcome to MolGLide. Click File -> New to open a new project")

        ImGui.separatorText("Graphics Options")
        ImGui.sliderInt("MSAA Samples (Hardware Restricted)", MSAACount, 2, highestSamples)
        if (ImGui.isItemHovered()) {
            ImGui.setTooltip("Multi Sampled Antialiasing makes certain lines and text smoother, but comes at a performance cost. The maximum sample size is limited by hardware")
        }

        if (ImGui.sliderFloat("ImGui Global Scale", globalScale, 0.0f, 5.0f)) {
            ImGui.getIO().fontGlobalScale = globalScale[0]
        }

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

            versionIndex("MolGLide version", MolGLide.VERSION)
            versionIndex("OpenGL version", GL11.glGetString(GL11.GL_VERSION))
            versionIndex("LWJGL version", getVersion())
            versionIndex("GLFW version", GLFW.glfwGetVersionString())
            versionIndex("Dear ImGui version", ImGui.getVersion())
            versionIndex("CDK version", CDK.getVersion())
            versionIndex("JRE version", Runtime.version().toString())
            versionIndex("Platform", "${Platform.get()} ${Platform.getArchitecture()}")

            if (extendedOpenGL) {
                versionIndex("GLSL version", GL11.glGetString(GL30.GL_SHADING_LANGUAGE_VERSION))
                versionIndex("OpenGL Vendor", GL11.glGetString(GL11.GL_VENDOR))
                versionIndex("OpenGL Renderer", GL11.glGetString(GL11.GL_RENDERER))
                versionIndex("Max Texture Samples", "${GL11.glGetInteger(GL32.GL_MAX_COLOR_TEXTURE_SAMPLES)}")
                versionIndex("Max Depth Samples", "${GL11.glGetInteger(GL32.GL_MAX_DEPTH_TEXTURE_SAMPLES)}")
                versionIndex("Max Texture Samples", "${GL11.glGetInteger(GL32.GL_MAX_TEXTURE_UNITS)}")
                versionIndex("Max Geometry vertices outputs", "${GL11.glGetInteger(GL32.GL_MAX_GEOMETRY_OUTPUT_VERTICES)}")
            }
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


        if (ImGui.checkbox("More OpenGL Info", extendedOpenGL)) {
            extendedOpenGL = !extendedOpenGL
        }

        ImGui.end()

    }

    fun getSamples() : Int {
        return MSAACount[0]
    }


    private fun versionIndex(component: String, version: String?) {
        ImGui.tableNextRow()
        ImGui.tableNextColumn()
        ImGui.text(component)
        ImGui.tableNextColumn()
        ImGui.text(version)
    }
}