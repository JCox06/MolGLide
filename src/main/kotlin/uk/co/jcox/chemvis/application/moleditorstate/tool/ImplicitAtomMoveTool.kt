package uk.co.jcox.chemvis.application.moleditorstate.tool

import uk.co.jcox.chemvis.application.moleditorstate.BondOrder.Companion.standardIncrements
import uk.co.jcox.chemvis.application.moleditorstate.BondOrder.SINGLE
import uk.co.jcox.chemvis.application.graph.ChemAtom
import uk.co.jcox.chemvis.application.graph.LevelContainer
import uk.co.jcox.chemvis.application.moleditorstate.ActionManager
import uk.co.jcox.chemvis.application.moleditorstate.SelectionManager
import uk.co.jcox.chemvis.cvengine.Camera2D
import uk.co.jcox.chemvis.cvengine.IRenderTargetContext
import uk.co.jcox.chemvis.cvengine.IResourceManager
import uk.co.jcox.chemvis.cvengine.InputManager

class ImplicitAtomMoveTool(toolboxContext: ToolboxContext,
                           renderingContext: IRenderTargetContext,
                           inputManager: InputManager,
                           camera2D: Camera2D,
                           levelContainer: LevelContainer,
                           selectionManager: SelectionManager,
                           actionManager: ActionManager) :
    Tool(toolboxContext, renderingContext, inputManager, camera2D, levelContainer, selectionManager, actionManager) {



    override fun onClick(clickX: Float, clickY: Float) {

        val selectedAtom = selectionManager.primarySelection

        if (selectedAtom is SelectionManager.Type.Active) {
            cycleGroupPosition(selectedAtom.atom)
        }
    }


    private fun cycleGroupPosition(atom: ChemAtom) {
        val posType = atom.implicitHydrogenPos
        val next = getNext(posType)
        atom.implicitHydrogenPos = next
    }

    override fun onRelease(clickX: Float, clickY: Float) {

    }

    override fun renderTransients(resourceManager: IResourceManager) {
        renderTransientSelectionMarker(resourceManager)
    }

    override fun update() {

    }


    companion object {
        val implicitOrder = listOf(ChemAtom.RelationalPos.LEFT, ChemAtom.RelationalPos.RIGHT, ChemAtom.RelationalPos.ABOVE, ChemAtom.RelationalPos.BOTTOM)

        fun getNext(pos: ChemAtom.RelationalPos) : ChemAtom.RelationalPos {
            val index = implicitOrder.indexOf(pos)
            val newIndex = index + 1
            if (newIndex < 0 || newIndex > implicitOrder.lastIndex) {
                return ChemAtom.RelationalPos.LEFT
            }
            return implicitOrder[newIndex]
        }
    }
}