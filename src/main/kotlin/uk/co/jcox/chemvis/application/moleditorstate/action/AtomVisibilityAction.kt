package uk.co.jcox.chemvis.application.moleditorstate.action

import uk.co.jcox.chemvis.application.graph.ChemAtom
import uk.co.jcox.chemvis.application.graph.LevelContainer

class AtomVisibilityAction (
    val atom: ChemAtom,
) : IAction {


    override fun execute(levelContainer: LevelContainer) {
        atom.visible = !atom.visible
    }

    override fun undo(levelContainer: LevelContainer) {
        execute(levelContainer)
    }
}