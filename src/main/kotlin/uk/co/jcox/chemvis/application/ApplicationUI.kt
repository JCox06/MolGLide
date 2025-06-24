package uk.co.jcox.chemvis.application

import imgui.ImGui
import imgui.ImVec2
import imgui.ImVec4
import imgui.flag.ImGuiCol
import imgui.flag.ImGuiCond
import imgui.flag.ImGuiStyleVar
import org.joml.Vector2f
import org.joml.Vector3f
import org.lwjgl.opengl.GL11
import uk.co.jcox.chemvis.application.moleditor.AtomInsert
import uk.co.jcox.chemvis.application.moleditor.NewOrganicEditorState
import uk.co.jcox.chemvis.application.moleditor.Utils
import uk.co.jcox.chemvis.cvengine.ICVServices
import java.awt.Desktop
import java.net.URI
import javax.swing.JFileChooser

class ApplicationUI (
    private val globalAppState: GlobalAppState,
    private val services: ICVServices
) {

    private val tools = listOf("Atom Bond Tool", "Atom Info Tool", "Elemental Edit Tool")
    private var activeTool = 0


    private var activeElement = 0

    private var activeState: NewOrganicEditorState? = null
    private var activeStateID: String? = null

    private var showMetricsWindow = false
    private var showStyleEditor = false

    private var showScreenshotWindow = false
    private val ssBondColour = FloatArray(3)
    private val ssWidth = FloatArray(1)
    private val ssTextColour = FloatArray(3)
    private val ssBackgroundColour = FloatArray(4)

    private val renderTargets = mutableListOf<String>()

    private var stateToScreenshot: NewOrganicEditorState? = null


    fun drawMainMenu() {
        val dockID = ImGui.dockSpaceOverViewport()
        drawMenuBar(dockID)

        drawRenderTargets(dockID)


        showWelcome(dockID)

        showGeneralWidgets()

        showScreenshotWindow()
    }


    private fun drawMenuBar(dockID: Int) {
        if (ImGui.beginMainMenuBar()) {

            drawFileMenu()
            drawEditMenu()
            drawHelpMenu()

            if (ImGui.button("Enter/Exit Screenshot mode")) {
                toggleScreenshotMode()
            }

            ImGui.separator()

            drawToolsMenu()
            drawToolOptions(activeTool)

            ImGui.separator()

            drawStateInfo()

            ImGui.endMainMenuBar()
        }
    }

    private fun drawRenderTargets(dockID: Int) {
        ImGui.pushStyleVar(ImGuiStyleVar.WindowPadding, ImVec2(0.0f, 0.0f))

        for (stateID in renderTargets) {
            val renderTarget = services.resourceManager().getRenderTarget(stateID)
            val renderingContext = services.getAppStateRenderingContext(stateID)
            ImGui.setNextWindowDockID(dockID, ImGuiCond.FirstUseEver)
            ImGui.begin(stateID)

            val winPos = ImGui.getWindowPos()
            renderingContext?.setImGuiWinMetrics(Vector2f(winPos.x, winPos.y))

            if (ImGui.isWindowHovered()) {
                services.resumeAppState(stateID)
                val state = services.getState(stateID)
                if (state is NewOrganicEditorState) {
                    activeState = state
                    activeStateID = stateID
                }
            } else {
                services.pauseAppState(stateID)
            }

            val width = ImGui.getContentRegionAvailX()
            val height = ImGui.getContentRegionAvailY()

            services.resourceManager().resizeRenderTarget(stateID, width, height)

            ImGui.image(renderTarget.colourAttachmentTexture.toLong(), ImVec2(width, height), ImVec2(0.0f, 1.0f), ImVec2(1.0f, 0.0f))

            renderingContext?.recalculate()
            ImGui.end()
        }
        ImGui.popStyleVar()
    }


    private fun drawFileMenu() {

        if (ImGui.beginMenu("File")) {

            if (ImGui.menuItem("New")) {
                val id = globalAppState.createOrganicEditor()
                renderTargets.add(id)
            }

            if (ImGui.menuItem("Save")) {
                //Handle logic for save
            }

            if (ImGui.menuItem("Save as")) {
                //Handle logic for save as
            }

            if (ImGui.menuItem("Visit website")) {
                Desktop.getDesktop().browse(URI("https://github.com/JCox06/MolGLide/tree/master"))
            }

            if (ImGui.menuItem("Close current window")) {
                activeStateID?.let {
                    if (renderTargets.contains(it)) {
                        globalAppState.closeOrganicEditor(it)
                        renderTargets.remove(it)
                    }

                }

            }

            if (ImGui.menuItem("Quit")) {
                services.shutdown()
            }

            ImGui.endMenu()
        }
    }

    private fun drawEditMenu() {
        if (ImGui.beginMenu("Edit")) {

            if (ImGui.menuItem("Undo")) {
                activeState?.undo()
            }

            if (ImGui.menuItem("Redo")) {
                activeState?.redo()
            }

            ImGui.endMenu()
        }
    }


    private fun drawHelpMenu() {
        if (ImGui.beginMenu("Help")) {

            if (ImGui.menuItem("ImGui Style Editor", showStyleEditor)) {
                showStyleEditor = !showStyleEditor
            }

            if ((ImGui.menuItem("ImGui Metrics", showMetricsWindow))) {
                showMetricsWindow = !showMetricsWindow
            }

            if (ImGui.menuItem("Wiki")) {
                //Handle logic for Wiki
            }

            if (ImGui.menuItem("About")) {
                //Handle logic for About
            }

            ImGui.endMenu()
        }
    }


    private fun drawToolsMenu() {
        if (ImGui.beginMenu("Tools")) {

            for ((index, toolName) in tools.withIndex()) {
                val enabled = index == activeTool
                if (ImGui.menuItem(toolName, enabled)) {
                    activeTool = index
                }
            }
            ImGui.endMenu()
        }
    }

    private fun drawToolOptions(toolID: Int) {
        if (toolID == 0) {
            renderButtons(listOf("C", "H", "O", "N", "P", "S", "F", "Cl", "Br", "I"))
        }
    }

    private fun renderButtons(elements: List<String>) {

        for ((index, string) in elements.withIndex()) {
            if (index == activeElement) {
                ImGui.pushStyleColor(ImGuiCol.Button, ImVec4(0.0f, 100.0f, 0.0f, 255.0f))
                ImGui.button(string, 40.0f, 25.0f)
                ImGui.popStyleColor()
            } else {
                if (ImGui.button(string, 30.0f, 25.0f)) {
                    activeElement = index
                    activeState?.atomInsert = AtomInsert.fromSymbol(string)
                }
            }
        }
    }

    private fun showGeneralWidgets() {
        if (showStyleEditor) {
            ImGui.showStyleEditor()
        }

        if (showMetricsWindow) {
            ImGui.showMetricsWindow()
        }
    }

    private fun drawStateInfo() {
        ImGui.text("Formula ${activeState?.moformula}")
    }

    private fun showWelcome(dockTo: Int) {
        ImGui.setNextWindowDockID(dockTo)

        ImGui.begin("Welcome")

        ImGui.text("Thanks for downloading and trying MolGLide")

        ImGui.bulletText("Click File-> New to get started")
        ImGui.bulletText("Select AtomBondTool from the available tools")
        ImGui.bulletText("Then select the element you wish to add")

        ImGui.end()
    }


    private fun toggleScreenshotMode() {
        showScreenshotWindow = !showScreenshotWindow

        stateToScreenshot = activeState

        if (!showScreenshotWindow) {
            stateToScreenshot?.readOnly = false
            stateToScreenshot?.undo()
            stateToScreenshot = null
            return
        }

        stateToScreenshot?.let {
            val bond = it.getBondStyle()
            ssBondColour[0] = bond.x
            ssBondColour[1] = bond.y
            ssBondColour[2] = bond.z
            ssWidth[0] = bond.w

            val text = it.getTextStyle()
            ssTextColour[0] = text.x
            ssTextColour[1] = text.y
            ssTextColour[2] = text.z

            it.makeCheckpoint()
        }
    }

    private fun showScreenshotWindow() {

        if (stateToScreenshot == null || !showScreenshotWindow) {
            return
        }

        stateToScreenshot?.readOnly = true

        ImGui.begin("Screenshot Settings")

        ImGui.text("Configuring screenshot for $stateToScreenshot")

        ImGui.colorPicker4("Background Colour", ssBackgroundColour)
        ImGui.colorPicker3("Text Colour", ssTextColour)
        ImGui.colorPicker3("Bond Colour", ssBondColour)
        ImGui.sliderFloat("Bond Width", ssWidth, 0.0f, 5.0f)

        stateToScreenshot?.setThemeStyle(Vector3f(ssTextColour[0], ssTextColour[1], ssTextColour[2]), Vector3f(ssBondColour[0], ssBondColour[1], ssBondColour[2]), ssWidth[0])
        stateToScreenshot?.setBackgroundColour(ssBackgroundColour[0], ssBackgroundColour[1], ssBackgroundColour[2], ssBackgroundColour[3])

        if (ImGui.button("Save Image")) {
            val chooser = JFileChooser()
            val result = chooser.showOpenDialog(null)
            if (result == JFileChooser.APPROVE_OPTION) {
                val filePath = chooser.selectedFile

                activeStateID?.let {
                    Utils.saveBufferToImg(services.resourceManager().getRenderTarget(it), filePath)
                }
            }
        }

        ImGui.end()

    }
}