package uk.co.jcox.chemvis.application

import imgui.ImGui
import imgui.ImVec2
import imgui.ImVec4
import imgui.flag.ImGuiCol
import imgui.flag.ImGuiCond
import org.joml.Vector2f
import uk.co.jcox.chemvis.application.moleditor.AtomInsert
import uk.co.jcox.chemvis.application.moleditor.NewOrganicEditorState
import uk.co.jcox.chemvis.cvengine.ICVServices
import java.awt.Desktop
import java.net.URI

class ApplicationUI (
    private val globalAppState: GlobalAppState,
    private val services: ICVServices
) {

    private val tools = listOf("Atom Bond Tool", "Atom Info Tool", "Elemental Edit Tool")
    private var activeTool = 0


    private var activeElement = 0

    private var activeState: NewOrganicEditorState? = null
    private var activeStateID: String? = null

    private val renderTargets = mutableListOf<String>()


    fun drawMainMenu() {
        val dockID = ImGui.dockSpaceOverViewport()
        drawMenuBar(dockID)

        drawRenderTargets(dockID)
    }


    private fun drawMenuBar(dockID: Int) {
        if (ImGui.beginMainMenuBar()) {

            drawFileMenu()
            drawEditMenu()
            drawHelpMenu()
            ImGui.separator()

            drawToolsMenu()
            drawToolOptions(activeTool)

            ImGui.separator()

            drawStateInfo()

            ImGui.endMainMenuBar()

            showWelcome(dockID)
        }
    }

    private fun drawRenderTargets(dockID: Int) {
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
}