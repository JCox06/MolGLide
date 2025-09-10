package uk.co.jcox.chemvis.application.moleditorstate.action

import uk.co.jcox.chemvis.application.chemengine.BondOrder
import uk.co.jcox.chemvis.application.graph.ChemBond
import uk.co.jcox.chemvis.application.graph.ChemMolecule
import uk.co.jcox.chemvis.application.graph.LevelContainer

class IncrementBondOrderAction (val molecule: ChemMolecule, val bond: ChemBond) : IAction {


    override fun execute(levelContainer: LevelContainer) {

        val originalBondOrder = levelContainer.chemManager.getBondOrder(bond.molManagerLink)

        val newBondOrder = BondOrder.increment(originalBondOrder)


        levelContainer.chemManager.updateBondOrder(molecule.molManagerLink, bond.molManagerLink, newBondOrder)

        levelContainer.chemManager.recalculate(molecule.molManagerLink)

    }

    override fun undo(levelContainer: LevelContainer) {

        val newBondOrder = levelContainer.chemManager.getBondOrder(bond.molManagerLink)
        val originalBondOrder = BondOrder.decrement(newBondOrder)

        levelContainer.chemManager.updateBondOrder(molecule.molManagerLink, bond.molManagerLink, originalBondOrder)

        levelContainer.chemManager.recalculate(molecule.molManagerLink)
    }
}