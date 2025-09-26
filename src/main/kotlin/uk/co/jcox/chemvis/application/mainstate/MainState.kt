package uk.co.jcox.chemvis.application.mainstate

import org.joml.Vector2f
import uk.co.jcox.chemvis.application.graph.LevelRenderer
import uk.co.jcox.chemvis.application.graph.ThemeStyleManager
import uk.co.jcox.chemvis.application.moleditorstate.AtomInsert
import uk.co.jcox.chemvis.application.moleditorstate.OrganicEditorState
import uk.co.jcox.chemvis.application.moleditorstate.StereoChem
import uk.co.jcox.chemvis.application.moleditorstate.TemplateRingInsert
import uk.co.jcox.chemvis.application.moleditorstate.tool.ToolboxContext
import uk.co.jcox.chemvis.cvengine.ApplicationState
import uk.co.jcox.chemvis.cvengine.ICVServices
import uk.co.jcox.chemvis.cvengine.IRenderTargetContext
import uk.co.jcox.chemvis.cvengine.ImGuiRenderingContext
import uk.co.jcox.chemvis.cvengine.InputManager

class MainState (val services: ICVServices, renderContext: IRenderTargetContext) : ApplicationState(renderContext) {

    private var idCount = 0
    val editors = mutableListOf<String>()


    val themeStyleManager = ThemeStyleManager()
    private val levelRenderer = LevelRenderer(services.batchRenderer(), services.instancedRenderer(), services.resourceManager(), themeStyleManager)

    val toolboxContext = ToolboxContext(AtomInsert.CARBON, StereoChem.IN_PLANE, TemplateRingInsert.BENZENE)

    fun createNewEditor(samples: Int) : String {
       val newEditor = OrganicEditorState(services, ImGuiRenderingContext(), levelRenderer, toolboxContext)
        val stateRenderID = "Editor#${idCount++}"

        if (samples > 1) {
            services.resourceManager().createMultiSampledRenderTarget(stateRenderID, samples)
        } else {
            services.resourceManager().createRenderTarget(stateRenderID)
        }

        services.setApplicationState(newEditor, stateRenderID)

        editors.add(stateRenderID)

        return stateRenderID
    }


    fun closeEditor(id: String) {
        services.destroyAppState(id)
        editors.remove(id)
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