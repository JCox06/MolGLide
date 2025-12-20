package uk.co.jcox.chemvis.application.moleditorstate.action

import uk.co.jcox.chemvis.application.graph.ChemBond
import uk.co.jcox.chemvis.application.graph.LevelContainer
import uk.co.jcox.chemvis.application.moleditorstate.BondOrder
import uk.co.jcox.chemvis.application.moleditorstate.StereoChem

class ChangeBondOrderAction (val bond: ChemBond, val newOrder: BondOrder) : IAction {

    var oldOrder: BondOrder? = null

    override fun execute(levelContainer: LevelContainer) {
        oldOrder = levelContainer.chemManager.getBondOrder(bond.molManagerLink)
        levelContainer.chemManager.updateBondOrder(bond.atomA.parent.molManagerLink, bond.molManagerLink, newOrder)
        levelContainer.chemManager.recalculate(bond.atomA.parent.molManagerLink)
    }

    override fun undo(levelContainer: LevelContainer) {
        oldOrder?.let {
            levelContainer.chemManager.updateBondOrder(bond.atomA.parent.molManagerLink, bond.molManagerLink, it)
            levelContainer.chemManager.recalculate(bond.atomA.parent.molManagerLink)
        }
    }
}