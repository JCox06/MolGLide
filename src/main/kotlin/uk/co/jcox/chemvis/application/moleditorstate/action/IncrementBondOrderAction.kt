package uk.co.jcox.chemvis.application.moleditorstate.action

import org.apache.commons.math.geometry.Vector3D.dotProduct
import org.joml.Vector3f
import org.joml.minus
import org.joml.plus
import uk.co.jcox.chemvis.application.chemengine.BondOrder
import uk.co.jcox.chemvis.application.chemengine.IMoleculeManager
import uk.co.jcox.chemvis.application.graph.ChemAtom
import uk.co.jcox.chemvis.application.graph.ChemBond
import uk.co.jcox.chemvis.application.graph.ChemMolecule
import uk.co.jcox.chemvis.application.graph.LevelContainer
import uk.co.jcox.chemvis.application.moleditorstate.AtomInsert

class IncrementBondOrderAction (val molecule: ChemMolecule, val bond: ChemBond) : IAction {


    override fun execute(levelContainer: LevelContainer) {

        val originalBondOrder = levelContainer.chemManager.getBondOrder(bond.molManagerLink)

        val newBondOrder = BondOrder.increment(originalBondOrder)


        levelContainer.chemManager.updateBondOrder(molecule.molManagerLink, bond.molManagerLink, newBondOrder)

        levelContainer.chemManager.recalculate(molecule.molManagerLink)


        if (shouldCentreBond(levelContainer.chemManager, bond)) {
            bond.centredBond = true
        }

    }

    private fun shouldCentreBond(chemManager: IMoleculeManager, bond: ChemBond) : Boolean {

        //First check if type of atom is a heterobond (C to another element)
        val isHetero = isHeteroBond(chemManager, bond)

        if (!isHetero) {
            return false
        }

        //Now we know its a hetero bond (C to something else)
        //We can now check if there are at least two other bonds from carbon that are mirror in placement

        val checkMirror = checkMirrorBondPlacement(chemManager, bond)

        if (checkMirror) {
            return true
        }
        return false
    }


    private fun isHeteroBond(chemManager: IMoleculeManager, bond: ChemBond) : Boolean {
        val atomAElement = chemManager.getAtomInsert(bond.atomA.molManagerLink)
        val atomBElement = chemManager.getAtomInsert(bond.atomB.molManagerLink)

        if ((atomAElement == AtomInsert.CARBON && atomBElement != AtomInsert.CARBON ) || atomBElement == AtomInsert.CARBON && atomAElement != AtomInsert.CARBON) {
            return true
        }
        return false
    }

    private fun checkMirrorBondPlacement(chemManager: IMoleculeManager, bond: ChemBond) : Boolean{
        val atomAElement = chemManager.getAtomInsert(bond.atomA.molManagerLink)
        val atomBElement = chemManager.getAtomInsert(bond.atomB.molManagerLink)

        var carbonAtom = bond.atomA
        var otherAtom = bond.atomB
        if (atomBElement == AtomInsert.CARBON) {
            carbonAtom = bond.atomB
            otherAtom = bond.atomA
        }
        val allBonds = bond.atomA.parent.bonds
        val otherBonds = allBonds.filter {
            !( (it.atomA.molManagerLink == carbonAtom.molManagerLink && it.atomB.molManagerLink == otherAtom.molManagerLink) ||
                    (it.atomA.molManagerLink == otherAtom.molManagerLink && it.atomB.molManagerLink == carbonAtom.molManagerLink) )
        }

        if (otherBonds.size != 2) {
            return true
        }

        val otherAtoms = mutableListOf<ChemAtom>()
        otherBonds.forEach { bond ->
            if (bond.atomA == carbonAtom) {
                otherAtoms.add(bond.atomB)
            } else {
                otherAtoms.add(bond.atomA)
            }
        }

        if (otherAtoms.size != 2) {
            return true
        }

        val carbonPos = carbonAtom.getWorldPosition()
        val atomA = otherAtoms[0].getWorldPosition()
        val atomB = otherAtoms[1].getWorldPosition()

        val carbonToAtomA = (atomA - carbonPos).normalize()
        val carbonToAtomB = (atomB - carbonPos).normalize()

        bond.bisectorNudge = (carbonToAtomA + carbonToAtomB).normalize()

        val dotProduct = carbonToAtomA.dot(carbonToAtomB)

        if (dotProduct <= -0.30f) {
            return true
        }

        return false
    }

    override fun undo(levelContainer: LevelContainer) {

        val newBondOrder = levelContainer.chemManager.getBondOrder(bond.molManagerLink)
        val originalBondOrder = BondOrder.decrement(newBondOrder)

        levelContainer.chemManager.updateBondOrder(molecule.molManagerLink, bond.molManagerLink, originalBondOrder)

        levelContainer.chemManager.recalculate(molecule.molManagerLink)
    }
}