package uk.co.jcox.chemvis.application.moleditorstate.action

import org.apache.commons.math.geometry.Vector3D.dotProduct
import org.checkerframework.checker.units.qual.mol
import org.joml.Vector3f
import org.joml.minus
import org.joml.plus
import org.openscience.cdk.interfaces.IBond
import org.openscience.cdk.isomorphism.TransformOp
import uk.co.jcox.chemvis.application.graph.ChemAtom
import uk.co.jcox.chemvis.application.graph.ChemBond
import uk.co.jcox.chemvis.application.graph.ChemMolecule
import uk.co.jcox.chemvis.application.graph.LevelContainer
import uk.co.jcox.chemvis.application.moleditorstate.AtomInsert

class IncrementBondOrderAction (val molecule: ChemMolecule, val bond: ChemBond) : IAction {


    override fun execute(levelContainer: LevelContainer) {

        val originalBondOrder = bond.iBond.order
        val newBondOrder = increment(originalBondOrder)

        molecule.updateBondOrder(bond, newBondOrder)

        if (shouldCentreBond(bond)) {
            bond.centredBond = true
        }

    }

    private fun shouldCentreBond( bond: ChemBond) : Boolean {

        //First check if type of atom is a heterobond (C to another element)
        val isHetero = isHeteroBond(bond)

        if (!isHetero) {
            return false
        }

        //Now we know its a hetero bond (C to something else)
        //We can now check if there are at least two other bonds from carbon that are mirror in placement

        val checkMirror = checkMirrorBondPlacement(bond)

        if (checkMirror) {
            return true
        }
        return false
    }


    private fun isHeteroBond(bond: ChemBond) : Boolean {
        val atomAElement = AtomInsert.fromSymbol(bond.atomA.getSymbol())
        val atomBElement = AtomInsert.fromSymbol(bond.atomB.getSymbol())

        if ((atomAElement == AtomInsert.CARBON && atomBElement != AtomInsert.CARBON ) || atomBElement == AtomInsert.CARBON && atomAElement != AtomInsert.CARBON) {
            return true
        }
        return false
    }

    private fun checkMirrorBondPlacement(bond: ChemBond) : Boolean{
       //todo - Need to check this
        return true
    }

    override fun undo(levelContainer: LevelContainer) {

        val currentBondOrder = bond.iBond.order
        val originalOrder = decrement(currentBondOrder)

        molecule.updateBondOrder(bond, originalOrder)
    }

    private fun increment(order: IBond.Order) : IBond.Order {
        return when (order) {
            IBond.Order.SINGLE -> IBond.Order.DOUBLE
            IBond.Order.DOUBLE -> IBond.Order.TRIPLE
            IBond.Order.TRIPLE -> IBond.Order.SINGLE
            else -> IBond.Order.SINGLE
        }
    }

    private fun decrement(order: IBond.Order) : IBond.Order {
        return when (order) {
            IBond.Order.SINGLE -> IBond.Order.TRIPLE
            IBond.Order.DOUBLE -> IBond.Order.SINGLE
            IBond.Order.TRIPLE -> IBond.Order.DOUBLE
            else -> IBond.Order.SINGLE
        }
    }
}