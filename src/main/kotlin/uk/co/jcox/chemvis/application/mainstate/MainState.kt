package uk.co.jcox.chemvis.application.mainstate

import org.joml.Vector2f
import uk.co.jcox.chemvis.application.graph.LevelRenderer
import uk.co.jcox.chemvis.application.graph.ThemeStyle
import uk.co.jcox.chemvis.application.graph.ThemeStyleManager
import uk.co.jcox.chemvis.application.moleditorstate.OrganicEditorState
import uk.co.jcox.chemvis.cvengine.ApplicationState
import uk.co.jcox.chemvis.cvengine.ICVServices
import uk.co.jcox.chemvis.cvengine.IRenderTargetContext
import uk.co.jcox.chemvis.cvengine.ImGuiRenderingContext
import uk.co.jcox.chemvis.cvengine.InputManager
import java.util.logging.Level

class MainState (val services: ICVServices, renderContext: IRenderTargetContext) : ApplicationState(renderContext) {

    private var idCount = 0
    val editors = mutableListOf<String>()


    private val themeStyleManager = ThemeStyleManager()
    private val levelRenderer = LevelRenderer(services.batchRenderer(), services.instancedRenderer(), services.resourceManager(), themeStyleManager)

    fun createNewEditor() : String {
       val newEditor = OrganicEditorState(services, ImGuiRenderingContext(), levelRenderer)
        val stateRenderID = "Editor#${idCount++}"

        services.resourceManager().createMultiSampledRenderTarget(stateRenderID, 8)

        services.setApplicationState(newEditor, stateRenderID)

        editors.add(stateRenderID)

        return stateRenderID
    }


    fun closeEditor(id: String) {
        services.destroyAppState(id)
        editors.remove(id)
    }


    fun getCurrentTheme() : ThemeStyle {
        return themeStyleManager.activeTheme
    }

    override fun init() {
        themeStyleManager.applyMolGLideEdit()
    }
    override fun update(inputManager: InputManager, timeElapsed: Float) {

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