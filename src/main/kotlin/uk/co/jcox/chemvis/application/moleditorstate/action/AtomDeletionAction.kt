package uk.co.jcox.chemvis.application.moleditorstate.action

import org.openscience.cdk.graph.ConnectivityChecker
import org.openscience.cdk.ringsearch.RingSearch
import uk.co.jcox.chemvis.application.graph.ChemAtom
import uk.co.jcox.chemvis.application.graph.ChemBond
import uk.co.jcox.chemvis.application.graph.ChemMolecule
import uk.co.jcox.chemvis.application.graph.LevelContainer
import kotlin.io.encoding.Base64

class AtomDeletionAction (private val atomToDelete: ChemAtom) : IAction {

    private var deletionMethod: DeletionMethod = DeletionMethod.None

    override fun execute(levelContainer: LevelContainer) {
        val bondCount = getBondCount(atomToDelete)
        if (bondCount == 0) {
            val method = DeletionMethod.DiscreteDelete(atomToDelete.parent)
            deletionMethod = method
            deleteDiscreteAtom(levelContainer, method)

            return
        }

        if (bondCount == 1) {
            val bond = atomToDelete.parent.bonds.find { it.atomA == atomToDelete || it.atomB == atomToDelete }
            if (bond != null) {
                val method = DeletionMethod.TerminalDelete(atomToDelete.parent, atomToDelete, bond)
                deletionMethod = method
                deleteTerminalAtom(levelContainer, method)
            }

            return
        }
        if (bondCount > 1) { val bondsFound = atomToDelete.parent.bonds.filter { it.atomA == atomToDelete || it.atomB == atomToDelete }
            val method = DeletionMethod.PathDelete(atomToDelete.parent, atomToDelete, bondsFound, null)
            deletionMethod = method
            deletePathAtom(levelContainer, method)

            return
        }
    }

    override fun undo(levelContainer: LevelContainer) {
        when (val method = deletionMethod) {
            is DeletionMethod.None -> {}
            is DeletionMethod.DiscreteDelete -> restoreDiscreteAtom(levelContainer, method)
            is DeletionMethod.TerminalDelete -> restoreTerminalAtom(levelContainer, method)
            is DeletionMethod.PathDelete -> restorePathAtom(levelContainer, method)
        }
    }

    override fun redo(levelContainer: LevelContainer) {
        if (deletionMethod !is DeletionMethod.PathDelete) {
            super.redo(levelContainer)
        }

        //Special Redo is required so that the molecule reference is still valid after being removed, and previous undo and redo operations still work
        val method = deletionMethod
        if (method is DeletionMethod.PathDelete) {
            method.moleculeModified.removeAtom(method.atomRemoved)
            method.bondsRemoved.forEach { method.moleculeModified.removeBond(it) }

            levelContainer.sceneMolecules.remove((method.moleculeModified))
            method.fragmentsPlaced?.let { levelContainer.sceneMolecules.addAll(it)}
        }


    }

    private fun deleteDiscreteAtom(levelContainer: LevelContainer, method: DeletionMethod.DiscreteDelete) {
        levelContainer.sceneMolecules.remove(method.moleculeRemoved)
    }

    private fun restoreDiscreteAtom(levelContainer: LevelContainer, method: DeletionMethod.DiscreteDelete) {
        levelContainer.sceneMolecules.add(method.moleculeRemoved)
    }

    private fun deleteTerminalAtom(levelContainer: LevelContainer, method: DeletionMethod.TerminalDelete) {
        method.moleculeModified.removeBond(method.bondRemoved)
        method.moleculeModified.removeAtom(method.atomRemoved)
    }

    private fun restoreTerminalAtom(levelContainer: LevelContainer, method: DeletionMethod.TerminalDelete) {
        method.moleculeModified.addAtom(method.atomRemoved)
        method.moleculeModified.addBond(method.bondRemoved)
    }

    private fun deletePathAtom(levelContainer: LevelContainer, method: DeletionMethod.PathDelete) {
        method.moleculeModified.removeAtom(method.atomRemoved)
        method.bondsRemoved.forEach { method.moleculeModified.removeBond(it) }

        //Find fragments of the molecule that are no longer connected - If everything is connected just ignore
        if (method.moleculeModified.isFragmented()) {
            val fragments = method.moleculeModified.splitIntoFragments()
            levelContainer.sceneMolecules.remove(method.moleculeModified)
            levelContainer.sceneMolecules.addAll(fragments)
            method.fragmentsPlaced = fragments
        }
    }

    private fun restorePathAtom(levelContainer: LevelContainer, method: DeletionMethod.PathDelete) {
        method.moleculeModified.addAtom(method.atomRemoved)
        method.bondsRemoved.forEach { method.moleculeModified.addBond(it) }

        levelContainer.sceneMolecules.add((method.moleculeModified))
        method.fragmentsPlaced?.let { levelContainer.sceneMolecules.removeAll(it)}
    }

    //Returns the number of bonds, does not include implicit hydrogens!
    private fun getBondCount(atom: ChemAtom) : Int {
        return atomToDelete.parent.iContainer.getConnectedBondsCount(atomToDelete.iAtom)
    }


    private sealed class DeletionMethod {
        object None: DeletionMethod()
        class DiscreteDelete(val moleculeRemoved: ChemMolecule) : DeletionMethod() //Delete an atom if its the only atom in a molecule
        class TerminalDelete(val moleculeModified: ChemMolecule, val atomRemoved: ChemAtom, val bondRemoved: ChemBond) : DeletionMethod() //Delete an atom if its connected to a molecule by one bond
        class PathDelete(val moleculeModified: ChemMolecule, val atomRemoved: ChemAtom, val bondsRemoved: List<ChemBond>, var fragmentsPlaced: List<ChemMolecule>?) : DeletionMethod() //Delete an atom with two or more connections, where once removed, everythign is still connected through x or more bonds
        //^^^ Like removing an atom in a ring system, everything is still connected as part of the same molecule
    }
}