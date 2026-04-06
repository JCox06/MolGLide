package uk.co.jcox.chemvis.application.moleditorstate.tool

import org.apache.commons.lang3.mutable.Mutable
import org.joml.Vector3f
import org.openscience.cdk.math.Vector
import uk.co.jcox.chemvis.application.MolGLide
import uk.co.jcox.chemvis.application.graph.ChemAtom
import uk.co.jcox.chemvis.application.graph.LevelContainer
import uk.co.jcox.chemvis.application.moleditorstate.ActionManager
import uk.co.jcox.chemvis.application.moleditorstate.SelectionManager
import uk.co.jcox.chemvis.application.moleditorstate.action.ApplyAtomTranslationAction
import uk.co.jcox.chemvis.application.moleditorstate.tool.AtomBondTool.Mode
import uk.co.jcox.chemvis.application.ui.tool.ToolViewUI
import uk.co.jcox.chemvis.cvengine.Camera2D
import uk.co.jcox.chemvis.cvengine.ICVServices
import uk.co.jcox.chemvis.cvengine.IRenderTargetContext
import uk.co.jcox.chemvis.cvengine.InputManager
import uk.co.jcox.chemvis.cvengine.RawInput

class SelectionTool(toolViewUI: ToolViewUI,
                    renderingContext: IRenderTargetContext,
                    inputManager: InputManager,
                    camera2D: Camera2D,
                    levelContainer: LevelContainer,
                    selectionManager: SelectionManager,
                    actionManager: ActionManager
) : Tool<ToolViewUI>(toolViewUI,
    renderingContext, inputManager, camera2D, levelContainer, selectionManager, actionManager
) {

    private val selections: MutableList<ApplyAtomTranslationAction.AtomDragData> = mutableListOf()
    private var mode: SelectionMode = SelectionMode.None

    override fun onClick(clickX: Float, clickY: Float) {
        val primary = selectionManager.primarySelection
        if (primary is SelectionManager.Type.ActiveAtom) {
            val pos = primary.atom.getInnerPosition()
            selections.add(ApplyAtomTranslationAction.AtomDragData(primary.atom, pos.x, pos.y))
            mode = SelectionMode.Discrete
            return
        }

        val mousePos = mouseWorld()

        mode = SelectionMode.Rectangle(mousePos.x, mousePos.y, mousePos.x, mousePos.y)
        //
    }

    override fun onRelease(clickX: Float, clickY: Float) {
        if (mode == SelectionMode.Discrete) {
            println("COmmited Drag")
            commitAtomDrag()
            mode = SelectionMode.None
        }
        mode = SelectionMode.None
        selections.clear()
    }

    override fun update() {
        if (mode != SelectionMode.None) {
            updateAtomDrag()
        }
        val m = mode
        if (m is SelectionMode.Rectangle && inputManager.mouseClick(RawInput.MOUSE_1)) {
            updateBoundingBox(m)
        }
    }

    private fun updateBoundingBox(rectangle: SelectionMode.Rectangle) {
        val mouseWorld = mouseWorld()
        rectangle.endX = mouseWorld.x
        rectangle.endY = mouseWorld.y
    }

    private fun commitAtomDrag() {
        val action = ApplyAtomTranslationAction(selections.toList())
        actionManager.executeAction(action)
    }

    private fun updateAtomDrag() {

        selections.forEach { atomDragData ->
            val mouseWorld = mouseWorld()
            val molWorld = atomDragData.atom.parent.positionOffset
            val newX = mouseWorld.x - molWorld.x
            val newY = mouseWorld.y - molWorld.y

            atomDragData.atom.setInnerPosition(newX, newY)
        }
    }

    override fun onCustomTransientUIRender(services: ICVServices) {
        val m = mode
        if (m is SelectionMode.Rectangle) {
            val lineProgram = services.resourceManager().useProgram(MolGLide.SHADER_LINE)
            lineProgram.uniform("uWidthMod", 0)
            lineProgram.uniform("uDashed", 1)
            lineProgram.uniform("uLight", Vector3f(0.8f, 0.5f, 0.5f))

            val data = mutableListOf<Float>(
                m.startX, m.startY, -1.0f,  m.endX, m.startY, -1.0f, 1.0f, //Top line
                m.startX, m.endY, -1.0f,  m.endX, m.endY, -1.0f, 1.0f, //Bottom Line
                m.startX, m.startY, -1.0f,  m.startX, m.endY, -1.0f, 1.0f, //Left Line
                m.endX, m.startY, -1.0f,  m.endX, m.endY, -1.0f, 1.0f, //Right
            )
                val lineMesh = services.resourceManager().getMesh(MolGLide.MESH_HOLDER_LINE)

            services.instancedRenderer().drawMeshes(lineMesh, data)
        }
    }

    sealed class SelectionMode {
        object None: SelectionMode()
        object Discrete: SelectionMode()
        class Rectangle(val startX: Float, val startY: Float, var endX: Float, var endY: Float): SelectionMode()
    }
}