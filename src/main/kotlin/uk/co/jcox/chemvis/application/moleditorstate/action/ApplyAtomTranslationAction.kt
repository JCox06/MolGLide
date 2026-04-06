package uk.co.jcox.chemvis.application.moleditorstate.action

import uk.co.jcox.chemvis.application.graph.ChemAtom
import uk.co.jcox.chemvis.application.graph.LevelContainer

class ApplyAtomTranslationAction (
    private val atomDragDataList: List<AtomDragData>,
) : IAction{


    override fun execute(levelContainer: LevelContainer) {

        atomDragDataList.forEach { atomDragData ->
            if (atomDragData.firstLoopRun) {
                atomDragData.endX = atomDragData.atom.getInnerPosition().x
                atomDragData.endY = atomDragData.atom.getInnerPosition().y
                atomDragData.firstLoopRun = false
            }

            atomDragData.atom.setInnerPosition(atomDragData.endX, atomDragData.endY)
        }
    }

    override fun undo(levelContainer: LevelContainer) {
        println("Testing the undo")
        atomDragDataList.forEach { atomDragData ->
            atomDragData.atom.setInnerPosition(atomDragData.startX, atomDragData.startY)
        }
    }

    class AtomDragData(
        val atom: ChemAtom,
        val startX: Float,
        val startY: Float,
    ) {
        var endX: Float = 0.0f
        var endY: Float = 0.0f
        var firstLoopRun = true
    }
} 