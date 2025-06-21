package uk.co.jcox.chemvis.application

import imgui.ImGui
import imgui.ImVec2
import org.joml.Vector2f
import uk.co.jcox.chemvis.application.moleditor.NewOrganicEditorState
import uk.co.jcox.chemvis.cvengine.Camera2D
import uk.co.jcox.chemvis.cvengine.IApplicationState
import uk.co.jcox.chemvis.cvengine.ICVServices
import uk.co.jcox.chemvis.cvengine.InputManager

class GlobalAppState (val services: ICVServices, val camera2D: Camera2D) : IApplicationState {


    val statesToRender = mutableListOf<String>()
    var idCount = 0

    override fun init() {

    }

    override fun update(inputManager: InputManager, timeElapsed: Float) {

    }

    override fun render(viewport: Vector2f) {

        ImGui.beginMainMenuBar()

        if (ImGui.beginMenu("File")) {
            if (ImGui.menuItem("New")) {

                val newState = NewOrganicEditorState(services, camera2D, services.levelRenderer())
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

            ImGui.begin(stateID)

            ImGui.image(actualState.colourAttachmentTexture.toLong(), ImVec2(500f, 500f))

            ImGui.end()
        }
    }

    override fun cleanup() {

    }
}