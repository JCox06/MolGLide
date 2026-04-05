package uk.co.jcox.chemvis.application.moleditorstate.action

import uk.co.jcox.chemvis.application.graph.ChemAtom
import uk.co.jcox.chemvis.application.graph.ChemBond
import uk.co.jcox.chemvis.application.graph.ChemMolecule
import uk.co.jcox.chemvis.application.graph.LevelContainer

class BondDeletionAction (private val bondToDelete: ChemBond) : IAction {


    private var deletionMethod: DeletionMethod = DeletionMethod.None

    override fun execute(levelContainer: LevelContainer) {
        val molecule = bondToDelete.atomA.parent

        //Remove the bond:
        molecule.removeBond(bondToDelete)

        //Get fragments (if there are any)
        if (! molecule.isFragmented()) {
            deletionMethod = DeletionMethod.PathDelete(bondToDelete, molecule)
            return
        }

        val method = DeletionMethod.FragmentDelete(bondToDelete, molecule, null)
        deletionMethod = method
        handleFragmentFormation(levelContainer, method)
    }


    private fun handleFragmentFormation(levelContainer: LevelContainer, method: DeletionMethod.FragmentDelete) {
        val fragments = method.originalMolecule.splitIntoFragments()
        levelContainer.sceneMolecules.remove(method.originalMolecule)
        levelContainer.sceneMolecules.addAll(fragments)
        method.fragments = fragments
    }

    override fun undo(levelContainer: LevelContainer) {
        when (val method = deletionMethod) {
            is DeletionMethod.FragmentDelete -> {
                method.originalMolecule.addBond(method.bond)
                method.fragments?.let { levelContainer.sceneMolecules.removeAll(it) }
                levelContainer.sceneMolecules.add(method.originalMolecule)
            }
            is DeletionMethod.PathDelete -> {
                method.originalMolecule.addBond(method.bond)
            }
            else -> {}
        }
    }

    override fun redo(levelContainer: LevelContainer) {
        val method = deletionMethod
        if (method is DeletionMethod.FragmentDelete) {
            method.originalMolecule.removeBond(method.bond)
            method.fragments?.let { levelContainer.sceneMolecules.addAll(it) }
            levelContainer.sceneMolecules.remove(method.originalMolecule)
            return
        }
        super.redo(levelContainer)
    }

    private sealed class DeletionMethod {
        object None : DeletionMethod()
        class PathDelete(val bond: ChemBond, val originalMolecule: ChemMolecule): DeletionMethod() //Deleting does not affect the number of molecules
        class FragmentDelete(val bond: ChemBond, val originalMolecule: ChemMolecule, var fragments: List<ChemMolecule>?): DeletionMethod() //Deleting results in formation of two fragments
    }
}