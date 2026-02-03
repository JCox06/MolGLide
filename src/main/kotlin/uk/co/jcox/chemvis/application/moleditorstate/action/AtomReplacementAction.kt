package uk.co.jcox.chemvis.application.moleditorstate.action

import uk.co.jcox.chemvis.application.graph.ChemAtom
import uk.co.jcox.chemvis.application.graph.LevelContainer
import uk.co.jcox.chemvis.application.moleditorstate.AtomInsert

open class AtomReplacementAction (
    private val atom: ChemAtom,
    private val toReplace: String,
) : IAction {

    private var oldAtom = "C"

    override fun execute(levelContainer: LevelContainer) {
        val parent = atom.parent
        oldAtom = atom.getSymbol()
        parent.updateSymbol(atom, toReplace)
        atom.visible = toReplace != "C"
    }

    override fun undo(levelContainer: LevelContainer) {
        val parent = atom.parent
        parent.updateSymbol(atom, oldAtom)

        atom.visible = oldAtom != "C"
    }
}