package uk.co.jcox.chemvis.application

import imgui.ImGui
import imgui.ImVec2
import imgui.flag.ImGuiWindowFlags
import org.joml.Vector2f
import uk.co.jcox.chemvis.application.moleditor.NewOrganicEditorState
import uk.co.jcox.chemvis.cvengine.ApplicationState
import uk.co.jcox.chemvis.cvengine.Camera2D
import uk.co.jcox.chemvis.cvengine.ICVServices
import uk.co.jcox.chemvis.cvengine.InputManager
import uk.co.jcox.chemvis.cvengine.IRenderTargetContext
import uk.co.jcox.chemvis.cvengine.ImGuiRenderingContext

class GlobalAppState (val services: ICVServices, renderTargetContext: IRenderTargetContext) : ApplicationState(renderTargetContext) {


    val statesToRender = mutableListOf<String>()
    val camera = Camera2D(services.windowMetrics().x, services.windowMetrics().y)
    var idCount = 0

    override fun init() {

    }

    override fun update(inputManager: InputManager, timeElapsed: Float) {
        val wm = services.windowMetrics()
        camera.update(wm.x, wm.y)

        renderTargetContext.recalculate()
    }

    override fun render(viewport: Vector2f) {

        ImGui.beginMainMenuBar()

        if (ImGui.beginMenu("File")) {
            if (ImGui.menuItem("New")) {

                val newState = NewOrganicEditorState(services, services.levelRenderer(), ImGuiRenderingContext())
                val idName = "OrganicEditorState#${idCount++}"

                services.resourceManager().createRenderTarget(idName)
                services.setApplicationState(newState, idName)
                statesToRender.add(idName)
            }

            ImGui.endMenu()
        }
        ImGui.endMainMenuBar()


        ImGui.dockSpaceOverViewport()


        showStates()
    }


    private fun showStates() {
        statesToRender.forEach { stateID ->
            val actualState = services.resourceManager().getRenderTarget(stateID)

            ImGui.begin(stateID, ImGuiWindowFlags.MenuBar)

            services.getAppStateRenderingContext(stateID)?.recalculate()

            if (ImGui.isWindowHovered()) {
                services.resumeAppState(stateID)
            } else {
                services.pauseAppState(stateID)
            }

            ImGui.beginMenuBar()


            ImGui.endMenuBar()

            val width = ImGui.getWindowWidth()
            val height = ImGui.getWindowHeight()

            services.resourceManager().resizeRenderTarget(stateID, width, height)

            ImGui.image(actualState.colourAttachmentTexture.toLong(), ImVec2(width, height), ImVec2(0.0f, 1.0f), ImVec2(1.0f, 0.0f))

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