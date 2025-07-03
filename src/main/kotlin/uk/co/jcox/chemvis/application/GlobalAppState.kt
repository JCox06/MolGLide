package uk.co.jcox.chemvis.application

import org.joml.Vector2f
import uk.co.jcox.chemvis.application.moleditor.OrganicEditorState
import uk.co.jcox.chemvis.cvengine.ApplicationState
import uk.co.jcox.chemvis.cvengine.Camera2D
import uk.co.jcox.chemvis.cvengine.ICVServices
import uk.co.jcox.chemvis.cvengine.InputManager
import uk.co.jcox.chemvis.cvengine.IRenderTargetContext
import uk.co.jcox.chemvis.cvengine.ImGuiRenderingContext

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
        val newState = OrganicEditorState(services, ImGuiRenderingContext())
        val renderTargetID = "Editor#${idCount++}"
        services.resourceManager().createMultiSampledRenderTarget(renderTargetID, 12)
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