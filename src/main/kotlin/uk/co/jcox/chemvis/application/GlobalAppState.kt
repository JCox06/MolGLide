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

    val camera = Camera2D(services.windowMetrics().x, services.windowMetrics().y)
    var idCount = 0


    override fun init() {

    }

    override fun update(inputManager: InputManager, timeElapsed: Float) {
        val wm = services.windowMetrics()
        camera.update(wm.x, wm.y)
        renderTargetContext.recalculate()
    }

    fun createOrganicEditor() : String {
        val newState = NewOrganicEditorState(services, ImGuiRenderingContext())
        val renderTargetID = "Editor#${idCount++}"
        services.resourceManager().createRenderTarget(renderTargetID)
        services.setApplicationState(newState, renderTargetID)
        return renderTargetID
    }

    fun closeOrganicEditor(id: String) {
        services.destroyAppState(id)
    }

    override fun render(viewport: Vector2f) {

    }

    override fun cleanup() {

    }

    override fun onPause() {

    }

    override fun onResume() {

    }

}