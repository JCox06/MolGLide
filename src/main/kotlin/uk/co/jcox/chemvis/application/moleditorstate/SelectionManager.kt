package uk.co.jcox.chemvis.application.moleditorstate

import org.joml.Vector3f
import org.joml.minus
import org.joml.plus
import org.joml.unaryMinus
import uk.co.jcox.chemvis.application.graph.ChemAtom
import uk.co.jcox.chemvis.application.graph.ChemBond
import uk.co.jcox.chemvis.application.graph.ChemMolecule
import uk.co.jcox.chemvis.application.graph.LevelContainer

class SelectionManager {

    var primarySelection: Type = Type.None
        private set

    var lock = false


    fun update(levelContainer: LevelContainer, xPos: Float, yPos: Float) {
        if (lock) {
            return
        }
        primarySelection = findPrimarySelection(levelContainer, Vector3f(xPos, yPos, OrganicEditorState.ATOM_PLANE))
    }


    /**
     * Find the nearest atom or bond from a supplied world location
     * @param levelContainer the level to check
     * @param mouseWorld the world pos to test
     */
    private fun findPrimarySelection(levelContainer: LevelContainer, mouseWorld: Vector3f) : Type {

        val closestAtom = getNearestAtom(levelContainer, mouseWorld)
        val closestBond = getNearestBond(levelContainer, mouseWorld)

        if (closestAtom == null && closestBond != null && closestBond.second < MIN_SELECTION_DISTANCE) {
            return Type.ActiveBond(closestBond.first)
        }
        if (closestAtom != null && closestBond == null && closestAtom.second < MIN_SELECTION_DISTANCE) {
            return Type.ActiveAtom(closestAtom.first)
        }
        if (closestAtom != null && closestBond != null) {
            if (closestAtom.second < closestBond.second && closestAtom.second < MIN_SELECTION_DISTANCE) {
                return Type.ActiveAtom(closestAtom.first)
            }
            if (closestBond.second < MIN_SELECTION_DISTANCE) {
                return Type.ActiveBond(closestBond.first)
            }
        }

        return Type.None
    }

    private fun getNearestAtom(levelContainer: LevelContainer, mouseWorld: Vector3f) : Pair<ChemAtom, Float>? {
        val atoms = mutableListOf<ChemAtom>()
        levelContainer.sceneMolecules.forEach { mol ->
            atoms.addAll(mol.atoms)
        }
        val results = atoms.map { it to (it.getWorldPosition() - mouseWorld).length() }
        val result = results.minByOrNull { it.second }
        return result
    }

    private fun getNearestBond(levelContainer: LevelContainer, mouseWorld: Vector3f) : Pair<ChemBond, Float>? {
        val bonds = mutableListOf<ChemBond>()
        levelContainer.sceneMolecules.forEach { mol ->
            bonds.addAll(mol.bonds)
        }

        val results = bonds.map { it to ((it.getMidpoint() - mouseWorld).length()) }
        val result = results.minByOrNull { it.second }
        return result
    }


    fun getMoleculeSelection() : ChemMolecule? {
        val selection = primarySelection
        if (selection is Type.ActiveAtom) {
            return selection.atom.parent
        }
        if (selection is Type.ActiveBond) {
            return selection.bond.atomA.parent
        }

        return null
    }


    sealed class Type {
        object None: Type()
        data class ActiveAtom(val atom: ChemAtom) : Type()
        data class ActiveBond(val bond: ChemBond) : Type()
    }

    companion object {
        private const val MIN_SELECTION_DISTANCE = 30.0f
        private const val MIN_BOND_SELECTION_DISTANCE = MIN_SELECTION_DISTANCE *2f
    }
}
