package uk.co.jcox.chemvis.application

import imgui.ImGui
import imgui.ImVec2
import imgui.ImVec4
import imgui.flag.ImGuiCol
import imgui.flag.ImGuiCond
import imgui.flag.ImGuiWindowFlags
import org.joml.Vector2f
import uk.co.jcox.chemvis.application.moleditor.AtomInsert
import uk.co.jcox.chemvis.application.moleditor.NewOrganicEditorState
import uk.co.jcox.chemvis.cvengine.ApplicationState
import uk.co.jcox.chemvis.cvengine.Camera2D
import uk.co.jcox.chemvis.cvengine.ICVServices
import uk.co.jcox.chemvis.cvengine.InputManager
import uk.co.jcox.chemvis.cvengine.IRenderTargetContext
import uk.co.jcox.chemvis.cvengine.ImGuiRenderingContext
import java.awt.Desktop
import java.net.URI

class GlobalAppState (val services: ICVServices, renderTargetContext: IRenderTargetContext) : ApplicationState(renderTargetContext) {


    val statesToRender = mutableListOf<String>()
    val camera = Camera2D(services.windowMetrics().x, services.windowMetrics().y)
    var idCount = 0
    private val appUIState = GuiState(AtomInsert.CARBON)

    var dockID = 0


    var showImGuiAbout = false
    var showMolGLide = false


    var activeElement = 0

    override fun init() {

    }

    override fun update(inputManager: InputManager, timeElapsed: Float) {
        val wm = services.windowMetrics()
        camera.update(wm.x, wm.y)

        renderTargetContext.recalculate()
    }

    override fun render(viewport: Vector2f) {

        drawMainMenuBar()
        drawWidgets()
        showStates()
    }


    private fun showStates() {
        statesToRender.forEach { stateID ->
            val actualState = services.resourceManager().getRenderTarget(stateID)

            ImGui.setNextWindowDockID(dockID, ImGuiCond.FirstUseEver)
            ImGui.begin(stateID, ImGuiWindowFlags.MenuBar)

            val renderContext = services.getAppStateRenderingContext(stateID)


            val winPos = ImGui.getWindowPos()

            renderContext?.setImGuiWinMetrics(Vector2f(winPos.x, winPos.y))


            if (ImGui.isWindowHovered()) {
                services.resumeAppState(stateID)
            } else {
                services.pauseAppState(stateID)
            }


            val state = services.getState(stateID)

            showStateMenuBar(stateID, state)

            val width = ImGui.getContentRegionAvailX()
            val height = ImGui.getContentRegionAvailY()

            services.resourceManager().resizeRenderTarget(stateID, width, height)

            ImGui.image(actualState.colourAttachmentTexture.toLong(), ImVec2(width, height), ImVec2(0.0f, 1.0f), ImVec2(1.0f, 0.0f))

            renderContext?.recalculate()

            ImGui.end()
        }
    }

    private fun drawMainMenuBar() {
        dockID = ImGui.dockSpaceOverViewport()


        ImGui.beginMainMenuBar()

        if (ImGui.beginMenu("File")) {
            if (ImGui.menuItem("New")) {

                val newState = NewOrganicEditorState(services, appUIState, ImGuiRenderingContext())
                val idName = "OrganicEditor#${idCount++}"

                services.resourceManager().createRenderTarget(idName)
                services.setApplicationState(newState, idName)
                statesToRender.add(idName)
            }

            if (ImGui.menuItem("Open")) {

            }


            if (ImGui.menuItem("Visit Website")) {
                Desktop.getDesktop().browse(URI("https://github.com/JCox06/MolGLide/tree/master"))
            }

            ImGui.separator()

            if (ImGui.menuItem("Quit")) {
                services.shutdown()
            }

            ImGui.endMenu()

        }

        if (ImGui.beginMenu("Help")) {

            if (ImGui.menuItem("About ImGui")) {
                showImGuiAbout = !showImGuiAbout
            }

            if (ImGui.menuItem("About MolGLide")) {
                showMolGLide = !showMolGLide
            }

            ImGui.endMenu()
        }


        renderButtons(listOf("C", "H", "O", "N", "P", "S", "F", "Cl", "Br", "I", "Li", "Mg"))

        ImGui.endMainMenuBar()
    }


    private fun showStateMenuBar(appStateID: String, state: ApplicationState?) {
        ImGui.beginMenuBar()

        if (ImGui.beginMenu("File")) {

            if (ImGui.menuItem("Save")) {

            }

            if (ImGui.menuItem("Save as")) {

            }

            if (ImGui.menuItem("Save and exit")) {

            }

            if (ImGui.menuItem("Close")) {
                services.destroyAppState(appStateID)
                //todo fix and remove from rendered states
            }

            ImGui.endMenu()
        }

        if (ImGui.beginMenu("Edit")) {

            if (ImGui.menuItem("Undo")) {

            }

            if (ImGui.menuItem("Redo")) {

            }

            ImGui.endMenu()
        }

        ImGui.separator()

        if (state is NewOrganicEditorState) {
            ImGui.text(state.moformula)
        }

        ImGui.endMenuBar()
    }

    private fun renderButtons(elements: List<String>) {

        for ((index, string) in elements.withIndex()) {
            if (index == activeElement) {
                ImGui.pushStyleColor(ImGuiCol.Button, ImVec4(0.0f, 100.0f, 0.0f, 255.0f))
                ImGui.button(string, 40.0f, 25.0f)
                ImGui.popStyleColor()
            } else {
                if (ImGui.button(string, 30.0f, 25.0f)) {
                    val insert = AtomInsert.fromSymbol(string)
                    appUIState.insert = insert
                    activeElement = index
                }
            }
        }
    }


    private fun drawWidgets() {
        drawAboutMenu()
    }


    private fun drawAboutMenu() {
        if (showImGuiAbout) {
            ImGui.showAboutWindow()
        }

        if (showMolGLide) {
            ImGui.begin("MolGLide About")

            ImGui.separator()

            ImGui.text("Thank you for using MolGLide ${MolGLide.VERSION}")

            ImGui.end()
        }
    }

    override fun cleanup() {

    }

    override fun onPause() {

    }

    override fun onResume() {

    }

}