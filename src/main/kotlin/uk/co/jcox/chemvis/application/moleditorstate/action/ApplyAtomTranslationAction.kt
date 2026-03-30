package uk.co.jcox.chemvis.application.moleditorstate.action

import uk.co.jcox.chemvis.application.graph.ChemAtom
import uk.co.jcox.chemvis.application.graph.LevelContainer

class ApplyAtomTranslationAction (
    private val atom: ChemAtom,
    private val startX: Float,
    private val startY: Float,
    private val endX: Float,
    private val endY: Float,
) : IAction{

    override fun execute(levelContainer: LevelContainer) {
        atom.setInnerPosition(endX, endY)
    }

    override fun undo(levelContainer: LevelContainer) {
        atom.setInnerPosition(startX, startY)
    }
} 