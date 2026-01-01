package uk.co.jcox.chemvis.application.moleditorstate.action

import org.checkerframework.checker.units.qual.mol
import org.openscience.cdk.interfaces.IBond
import uk.co.jcox.chemvis.application.graph.ChemBond
import uk.co.jcox.chemvis.application.graph.LevelContainer

class ChangeBondOrderAction (val bond: ChemBond, val newOrder: IBond.Order) : IAction {

    var oldOrder: IBond.Order? = null

    override fun execute(levelContainer: LevelContainer) {
        oldOrder = bond.iBond.order
        val molecule = bond.atomA.parent
        molecule.updateBondOrder(bond, newOrder)
    }

    override fun undo(levelContainer: LevelContainer) {
        oldOrder?.let {
            val molecule = bond.atomA.parent
            molecule.updateBondOrder(bond, it)
        }
    }
}