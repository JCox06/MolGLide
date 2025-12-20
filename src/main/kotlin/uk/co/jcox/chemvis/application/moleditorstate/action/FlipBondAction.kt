package uk.co.jcox.chemvis.application.moleditorstate.action

import uk.co.jcox.chemvis.application.graph.ChemBond
import uk.co.jcox.chemvis.application.graph.LevelContainer

class FlipBondAction (
    val bond: ChemBond
) : IAction {

    override fun execute(levelContainer: LevelContainer) {
        bond.flipDoubleBond = !bond.flipDoubleBond
    }

    override fun undo(levelContainer: LevelContainer) {
        execute(levelContainer)
    }
}