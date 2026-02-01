package uk.co.jcox.chemvis.application.mainstate

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.commons.logging.Log
import org.apache.jena.atlas.io.IO
import org.joml.Vector2f
import org.tinylog.Logger
import uk.co.jcox.chemvis.application.ToolRegistry
import uk.co.jcox.chemvis.application.data.LevelLoader
import uk.co.jcox.chemvis.application.data.LevelSerializer
import uk.co.jcox.chemvis.application.graph.LevelContainer
import uk.co.jcox.chemvis.application.graph.LevelRenderer
import uk.co.jcox.chemvis.application.graph.ThemeStyleManager
import uk.co.jcox.chemvis.application.moleditorstate.AtomInsert
import uk.co.jcox.chemvis.application.moleditorstate.OrganicEditorState
import uk.co.jcox.chemvis.application.moleditorstate.tool.AtomBondTool
import uk.co.jcox.chemvis.application.moleditorstate.tool.CommonTemplateTool
import uk.co.jcox.chemvis.application.moleditorstate.tool.ImplicitAtomMoveTool
import uk.co.jcox.chemvis.application.ui.Icons
import uk.co.jcox.chemvis.application.ui.tool.AtomBondToolView
import uk.co.jcox.chemvis.application.ui.tool.CommonTemplateToolView
import uk.co.jcox.chemvis.application.ui.tool.ToolViewUI
import uk.co.jcox.chemvis.cvengine.ApplicationState
import uk.co.jcox.chemvis.cvengine.ICVServices
import uk.co.jcox.chemvis.cvengine.IRenderTargetContext
import uk.co.jcox.chemvis.cvengine.ImGuiRenderingContext
import uk.co.jcox.chemvis.cvengine.InputManager
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOError
import java.io.IOException
import java.io.ObjectOutputStream

class MainState (val services: ICVServices, renderContext: IRenderTargetContext) : ApplicationState(renderContext) {


    val toolRegistry: ToolRegistry = ToolRegistry()

    private var idCount = 0
    val editors = mutableListOf<String>()
    private val levelSerializer = LevelSerializer()

    var bulkOperationMode = false


    val themeStyleManager = ThemeStyleManager()
    private val levelRenderer = LevelRenderer(services.batchRenderer(), services.instancedRenderer(), themeStyleManager, services.resourceManager())

    fun createNewEditor(samples: Int, levelContainer: LevelContainer = LevelContainer(), projectFile: File? = null) : String {
       val newEditor = OrganicEditorState(services, ImGuiRenderingContext(), levelRenderer, themeStyleManager, levelContainer)
        val stateRenderID = "Editor#${idCount++}"

        newEditor.projectFile = projectFile

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

        //The enigne will handle cleaning up the associated render target
        //Removing the render target here will cause a crash as its already been done at this point
    }

    fun saveProjectToFile(session: OrganicEditorState, file: File) {
        bulkOperationMode = true
        Logger.info { "Saving project to ${file.absoluteFile}" }

        val levelContainerToSave = session.levelContainer
        val dataContainer = levelSerializer.getDataLevel(levelContainerToSave)

        session.actionManager.markNotDirty()
        session.actionManager.clearHistory()

        services.getMainEngineScope().launch (Dispatchers.IO) {
            try {
                val fileOutput = FileOutputStream(file)
                ObjectOutputStream(fileOutput).use {
                    it.writeObject(dataContainer)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }

            bulkOperationMode = false
        }
    }


    fun openProject(file: File, samples: Int) {

        bulkOperationMode = true

        val levelLoader = LevelLoader()
        if (!file.exists()) {
            return
        }

        services.getMainEngineScope().launch(Dispatchers.IO) {
            val levelContainer = levelLoader.loadLevel(file)
            services.getMainEngineScope().launch {
                Logger.info { "Creating OrganicEditorState and associated RenderTarget" }
                createNewEditor(samples, levelContainer, file)
                bulkOperationMode = false
            }
        }
    }

    override fun init() {
        themeStyleManager.applyMolGLideEdit()

        //Register all the tools:
        registerTools()
    }


    private fun registerTools() {
        val atomBondToolView = AtomBondToolView()
        toolRegistry.registerTool("atom_bond_tool", "${Icons.ATOM_BOND_TOOL_ICON} Atom Bond Tool", atomBondToolView) { state ->
            return@registerTool AtomBondTool(atomBondToolView, state.renderingContext, services.inputs(), state.camera, state.levelContainer, state.selectionManager, state.actionManager)
        }

        val commonTemplateToolView = CommonTemplateToolView()
        toolRegistry.registerTool("common_template_tool", "${Icons.TEMPLATE_TOOL_ICON} Template Tool", commonTemplateToolView) { state ->
            return@registerTool CommonTemplateTool(commonTemplateToolView, state.renderingContext, services.inputs(), state.camera, state.levelContainer, state.selectionManager, state.actionManager)
        }

        val implicitGroupMoveView = ToolViewUI()
        toolRegistry.registerTool("implicit_group_move_tool", "${Icons.MOVE_ICON} Implicit Move Tool", implicitGroupMoveView) { state ->
            return@registerTool ImplicitAtomMoveTool(commonTemplateToolView, state.renderingContext, services.inputs(), state.camera, state.levelContainer, state.selectionManager, state.actionManager)
        }
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