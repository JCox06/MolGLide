package uk.co.jcox.chemvis.application.moleditorstate.action

import uk.co.jcox.chemvis.application.graph.ChemAtom
import uk.co.jcox.chemvis.application.graph.LevelContainer

class InsertCustomInputAction (
    private val chemAtom: ChemAtom,
    private val input: String,
) : IAction {

    override fun execute(levelContainer: LevelContainer) {

    }

    override fun undo(levelContainer: LevelContainer) {

    }
}