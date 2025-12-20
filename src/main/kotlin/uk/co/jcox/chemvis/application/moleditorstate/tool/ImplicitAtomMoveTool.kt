package uk.co.jcox.chemvis.application.moleditorstate.tool

import uk.co.jcox.chemvis.application.graph.ChemAtom
import uk.co.jcox.chemvis.application.graph.LevelContainer
import uk.co.jcox.chemvis.application.moleditorstate.ActionManager
import uk.co.jcox.chemvis.application.moleditorstate.SelectionManager
import uk.co.jcox.chemvis.application.ui.tool.ToolViewUI
import uk.co.jcox.chemvis.cvengine.Camera2D
import uk.co.jcox.chemvis.cvengine.IRenderTargetContext
import uk.co.jcox.chemvis.cvengine.IResourceManager
import uk.co.jcox.chemvis.cvengine.InputManager

class ImplicitAtomMoveTool(
                            simpleToolView: ToolViewUI,
                           renderingContext: IRenderTargetContext,
                           inputManager: InputManager,
                           camera2D: Camera2D,
                           levelContainer: LevelContainer,
                           selectionManager: SelectionManager,
                           actionManager: ActionManager) :
    Tool<ToolViewUI>(simpleToolView, renderingContext, inputManager, camera2D, levelContainer, selectionManager, actionManager) {



    override fun onClick(clickX: Float, clickY: Float) {

        val selectedAtom = selectionManager.primarySelection

        if (selectedAtom is SelectionManager.Type.ActiveAtom) {
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

    override fun update() {

    }


    /**
     * This tool does not affect the bonds only the atoms
     * Therefore we do not want to draw selection markers over the bonds
     * or allow the right click menu to show for bonds
     */
    override fun allowIndividualBondInteractions(): Boolean {
        return false
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