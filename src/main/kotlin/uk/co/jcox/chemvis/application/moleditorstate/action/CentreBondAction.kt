package uk.co.jcox.chemvis.application.moleditorstate.action

import uk.co.jcox.chemvis.application.graph.ChemBond
import uk.co.jcox.chemvis.application.graph.LevelContainer

class CentreBondAction (val bond: ChemBond) : IAction {

    override fun execute(levelContainer: LevelContainer) {
        bond.centredBond = !bond.centredBond
    }

    override fun undo(levelContainer: LevelContainer) {
        execute(levelContainer)
    }
}