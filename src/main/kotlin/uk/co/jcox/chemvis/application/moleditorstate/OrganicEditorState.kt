package uk.co.jcox.chemvis.application.moleditorstate

import org.joml.Matrix4f
import org.joml.Vector2f
import org.joml.Vector3f
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL30
import uk.co.jcox.chemvis.application.MolGLide
import uk.co.jcox.chemvis.application.graph.ChemMolecule
import uk.co.jcox.chemvis.application.graph.LevelContainer
import uk.co.jcox.chemvis.application.graph.LevelRenderer
import uk.co.jcox.chemvis.application.moleditorstate.tool.Tool
import uk.co.jcox.chemvis.application.ui.tool.ToolViewUI
import uk.co.jcox.chemvis.cvengine.ApplicationState
import uk.co.jcox.chemvis.cvengine.CVEngine
import uk.co.jcox.chemvis.cvengine.Camera2D
import uk.co.jcox.chemvis.cvengine.ICVServices
import uk.co.jcox.chemvis.cvengine.IInputSubscriber
import uk.co.jcox.chemvis.cvengine.IRenderTargetContext
import uk.co.jcox.chemvis.cvengine.IResourceManager
import uk.co.jcox.chemvis.cvengine.InputManager
import uk.co.jcox.chemvis.cvengine.RawInput
import java.io.File

class OrganicEditorState (
    val services: ICVServices,
    val renderingContext: IRenderTargetContext,
    val renderer: LevelRenderer,
    val levelContainer: LevelContainer = LevelContainer()
) : ApplicationState(renderingContext), IInputSubscriber {

    val actionManager = ActionManager(levelContainer)
    val camera = Camera2D(renderingContext.getWidth().toInt(), renderingContext.getHeight().toInt())
    val selectionManager = SelectionManager()
    val clickMenu = ClickMenu(selectionManager, actionManager, levelContainer, services.getMainEngineScope())

    var projectFile: File? = null

    var currentTool: Tool<out ToolViewUI>? = null

    override fun init() {

    }

    override fun update(inputManager: InputManager, timeElapsed: Float) {
        camera.update(renderTargetContext.getWidth().toInt(), renderingContext.getHeight().toInt())

        if (!inputManager.mouseClick(RawInput.MOUSE_1)) {
            val mousePos = camera.screenToWorld(renderTargetContext.getMousePos(inputManager))
            selectionManager.update(levelContainer, mousePos.x, mousePos.y)
        }

        currentTool?.update()
    }

    override fun render(viewport: Vector2f) {
        GL11.glViewport(0, 0, renderTargetContext.getWidth().toInt(), renderTargetContext.getHeight().toInt())

        currentTool?.let { renderSelectionTransients(it) }

        renderer.renderLevel(this.levelContainer, camera, viewport)

        clickMenu.renderMenu()
    }

    private fun renderSelectionTransients(transientTool: Tool<out ToolViewUI>) {

        val selection = selectionManager.primarySelection

        if (selection is SelectionManager.Type.ActiveAtom && transientTool.allowIndividualAtomInteractions()) {
            renderTransientSelectionMarker(services.resourceManager(), selection.atom.getWorldPosition(), true)
        }
        if (selection is SelectionManager.Type.ActiveBond && transientTool.allowIndividualBondInteractions()) {
            renderTransientSelectionMarker(services.resourceManager(), selection.bond.getMidpoint(), false)
        }
    }


    //todo This should be moved into a separate transientUIRenderer at some point
    //Its also a bit weird as it should go through the batch renderer rather than using pure OpenGL functions
    private fun renderTransientSelectionMarker(resourceManager: IResourceManager, position: Vector3f, circle: Boolean) {
        val objectProgram = resourceManager.useProgram(CVEngine.SHADER_SIMPLE_TEXTURE)
        objectProgram.uniform("uPerspective", camera.combined())
        objectProgram.uniform("uIgnoreTextures", 1)

        var drawingMode = GL11.GL_TRIANGLES
        var meshID = MolGLide.BOND_MARKER_MESH
        var scaleMod = 0.3f
        if (circle) {
            drawingMode = GL11.GL_TRIANGLE_FAN
            meshID = MolGLide.SELECTION_MARKER_MESH
            scaleMod = 0.7f
        }

        val mesh = resourceManager.getMesh(meshID)
        val material = resourceManager.getMaterial(MolGLide.SELECTION_MARKER_MATERIAL)

        objectProgram.uniform("uLight", material.colour)
        objectProgram.uniform("uModel",
            Matrix4f().translate(position.x, position.y, OrganicEditorState.MARKER_PLANE)
                .scale(MolGLide.FONT_SIZE * MolGLide.GLOBAL_SCALE * scaleMod)
        )

        GL30.glBindVertexArray(mesh.vertexArray)
        GL11.glDrawElements(drawingMode, mesh.vertices, GL11.GL_UNSIGNED_INT, 0)
        GL30.glBindVertexArray(0)

        objectProgram.uniform("uIgnoreTextures", 0)
    }

    override fun cleanup() {

    }

    override fun onPause() {
        services.inputs().unsubscribe(this)
    }

    override fun onResume() {
        services.inputs().subscribe(this)
    }

    override fun clickEvent(inputManager: InputManager, key: RawInput) {
        val mousePosScreen = renderingContext.getMousePos(inputManager)

        if (key == RawInput.MOUSE_1) {
            val mousePos = camera.screenToWorld(mousePosScreen)

            currentTool?.onClick(mousePos.x, mousePos.y)
        }

        //Check to see if the click menu should be opened
        if (selectionManager.primarySelection is SelectionManager.Type.ActiveBond && key == RawInput.MOUSE_2) {
            clickMenu.showBondMenu = true
        }
        if (selectionManager.primarySelection is SelectionManager.Type.ActiveAtom && key == RawInput.MOUSE_2) {
            clickMenu.showAtomMenu = true
        }

        //Check if CTRL key is being held down
        if (inputManager.keyClick(RawInput.LCTRL)) {
            if (key == RawInput.KEY_Z) {
                undo()
            }
            if (key == RawInput.KEY_Y) {
                redo()
            }
        }

    }

    fun undo() {
        actionManager.undoLastAction()
    }

    fun redo() {
        actionManager.restoreLastAction()
    }

    override fun clickReleaseEvent(inputManager: InputManager, key: RawInput) {
        if (key == RawInput.MOUSE_1) {
            val mousePos = camera.screenToWorld(renderingContext.getMousePos(inputManager))
            currentTool?.onRelease(mousePos.x, mousePos.y)
        }
    }

    override fun mouseMoveEvent(inputManager: InputManager, xPos: Double, yPos: Double) {

        if (inputManager.mouseClick(RawInput.MOUSE_3)) {
            val delta = inputManager.deltaMousePos()

            val scale = 0.05f

            camera.cameraPosition.add(Vector3f(-delta.x * scale, delta.y * scale, 0.0f))
        }
    }

    override fun mouseScrollEvent(inputManager: InputManager, xScroll: Double, yScroll: Double) {
        if (inputManager.keyClick(RawInput.LCTRL)) {
            this.camera.camWidth -= yScroll.toFloat() * 2;
        }
    }

    fun getFormula() : String {
        val mol = getSelectedMolecule()
        if (mol != null) {
            return mol.getFormulaString()
        }
        return "Nothing Selected"
    }

    fun getWeight() : Double {
        val mol = getSelectedMolecule()
        if (mol != null) {
            return mol.getMolecularWeight()
        }
        return 0.0
    }


    private fun getSelectedMolecule() : ChemMolecule? {
        return selectionManager.getMoleculeSelection()
    }




    companion object {
        const val ATOM_PLANE = -3.0f
        const val MARKER_PLANE = -2.0f
        const val CONNECTION_DISTANCE = 20.0f
        const val IMPLICIT_SCALE = 1.0f * MolGLide.FONT_SIZE * MolGLide.GLOBAL_SCALE

        const val MULTI_BOND_DISTANCE = 3.5f


        val COMMON_ANGLES = listOf<Float>(
            //Cardinal directions
            0.0f, 90.0f, -90.0f, 180.0f, -180.0f,

            //Semi Cardinal directions
            45.0f, 135.0f, -45.0f, -135.0f,

            //Odd angles - For triangles
            30.0f, -30.0f, 60.0f, -60.0f, 120.0f, -120.0f, 150.0f, -150.0f,

            //For Pentagons
            108.0f, -108.0f, 72.0f, -72.0f, 36.0f, -36.0f, 126.0f, -126.0f, 144.0f, -144.0f,
            18.0f, -18.0f, 162.0f, -162.0f, 126.0f, -126.0f, 36.0f, -36.0f, 54.0f, -54.0f,
            )
    }
}