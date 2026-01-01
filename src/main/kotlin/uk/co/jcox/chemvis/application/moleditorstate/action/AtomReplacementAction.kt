package uk.co.jcox.chemvis.application.moleditorstate.action

import uk.co.jcox.chemvis.application.graph.ChemAtom
import uk.co.jcox.chemvis.application.graph.LevelContainer
import uk.co.jcox.chemvis.application.moleditorstate.AtomInsert

class AtomReplacementAction (
    private val atom: ChemAtom,
    private val toReplace: AtomInsert,
) : IAction {

    private var oldAtom = AtomInsert.CARBON

    override fun execute(levelContainer: LevelContainer) {
        val parent = atom.parent
        parent.updateSymbol(atom, toReplace.symbol)
        atom.visible = toReplace != AtomInsert.CARBON
    }

    override fun undo(levelContainer: LevelContainer) {
        val parent = atom.parent
        parent.updateSymbol(atom, oldAtom.symbol)

        atom.visible = oldAtom != AtomInsert.CARBON
    }
}