package uk.co.jcox.chemvis.application.moleditorstate.action

import uk.co.jcox.chemvis.application.graph.ChemBond
import uk.co.jcox.chemvis.application.graph.LevelContainer

class ChangeAromacityAction (val bond: ChemBond) : IAction {

    override fun execute(levelContainer: LevelContainer) {
        val mol = bond.atomA.parent
        mol.setAromaticity(bond, !bond.iBond.isAromatic)
    }

    override fun undo(levelContainer: LevelContainer) {
        execute(levelContainer)
    }
}