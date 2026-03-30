package uk.co.jcox.chemvis.application.moleditorstate.tool

import org.joml.Vector3f
import org.joml.minus
import uk.co.jcox.chemvis.application.graph.ChemAtom
import uk.co.jcox.chemvis.application.graph.LevelContainer
import uk.co.jcox.chemvis.application.moleditorstate.ActionManager
import uk.co.jcox.chemvis.application.moleditorstate.SelectionManager
import uk.co.jcox.chemvis.application.moleditorstate.action.ApplyAtomTranslationAction
import uk.co.jcox.chemvis.application.ui.tool.ToolViewUI
import uk.co.jcox.chemvis.cvengine.Camera2D
import uk.co.jcox.chemvis.cvengine.IRenderTargetContext
import uk.co.jcox.chemvis.cvengine.InputManager

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

    private var mode: Mode = Mode.None

    override fun onClick(clickX: Float, clickY: Float) {
        val primary = selectionManager.primarySelection
        if (primary is SelectionManager.Type.ActiveAtom) {
            val pos = primary.atom.getInnerPosition()
            mode = Mode.AtomDrag(primary.atom, pos.x, pos.y)
        }
    }

    override fun onRelease(clickX: Float, clickY: Float) {
        when (val actualMode = mode) {
            is Mode.AtomDrag -> commitAtomDrag(actualMode)
            Mode.None -> {}
        }
    }

    override fun update() {
        when (val actualMode = mode) {
            is Mode.AtomDrag -> updateAtomDrag(actualMode)
            Mode.None -> {}
        }
    }

    private fun commitAtomDrag(atomDragMode: Mode.AtomDrag) {
        mode = Mode.None
        val currentPos = atomDragMode.atom.getInnerPosition()
        val action = ApplyAtomTranslationAction(atomDragMode.atom, atomDragMode.startX, atomDragMode.startY, currentPos.x, currentPos.y)
        actionManager.executeAction(action)
    }

    private fun updateAtomDrag(atomDragMode: Mode.AtomDrag) {
        val mouseWorld = mouseWorld()
        val molWorld = atomDragMode.atom.parent.positionOffset
        val newX = mouseWorld.x - molWorld.x
        val newY = mouseWorld.y - molWorld.y

        atomDragMode.atom.setInnerPosition(newX, newY)

    }

    sealed class Mode {
        object None : Mode()
        class AtomDrag(val atom: ChemAtom, val startX: Float, val startY: Float) : Mode()
    }
}