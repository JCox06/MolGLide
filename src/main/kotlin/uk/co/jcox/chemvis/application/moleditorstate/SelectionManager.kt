package uk.co.jcox.chemvis.application.moleditorstate

import org.joml.Vector3f
import org.joml.minus
import uk.co.jcox.chemvis.application.graph.ChemAtom
import uk.co.jcox.chemvis.application.graph.ChemBond
import uk.co.jcox.chemvis.application.graph.LevelContainer

class SelectionManager {

    var primarySelection: Type = Type.None
    private set

    var bondSelection: ChemBond? = null
    private set


    fun update(levelContainer: LevelContainer, xPos: Float, yPos: Float) {
        primarySelection = findPrimarySelection(levelContainer, Vector3f(xPos, yPos, OrganicEditorState.ATOM_PLANE))
        bondSelection = findBondSelection(levelContainer, Vector3f(xPos, yPos, OrganicEditorState.ATOM_PLANE))
    }


    /**
     * Find the nearest atom from a supplied world location
     * @param levelContainer the level to check
     * @param mouseWorld the world pos to test
     */
    private fun findPrimarySelection(levelContainer: LevelContainer, mouseWorld: Vector3f) : Type {
        var priSel: Type = Type.None

        levelContainer.sceneMolecules.forEach { mol ->
            mol.atoms.forEach { atom ->
                val worldPos = atom.getWorldPosition()

                val difference = worldPos - mouseWorld

                if (difference.length() <= MIN_SELECTION_DISTANCE) {
                    priSel = Type.Active(atom)
                    return@forEach
                }
            }
        }
        return priSel
    }


    private fun findBondSelection(levelContainer: LevelContainer, mouseWorld: Vector3f) : ChemBond? {
        var selection: ChemBond? = null

        levelContainer.sceneMolecules.forEach { mol ->
            mol.bonds.forEach { bond ->
                val worldPosAtomA = bond.atomA.getWorldPosition()
                val worldPosAtomB = bond.atomB.getWorldPosition()

                val differenceA = worldPosAtomA - mouseWorld
                val differenceB = worldPosAtomB - mouseWorld

                if (differenceA.length() <= MIN_BOND_SELECTION_DISTANCE && differenceB.length() <= MIN_BOND_SELECTION_DISTANCE) {
                    selection = bond
                }
            }
        }
        return selection
    }

    sealed class Type {
        object None: Type()
        data class Active(val atom: ChemAtom) : Type()
    }

    companion object {
        private const val MIN_SELECTION_DISTANCE = 30.0f
        private const val MIN_BOND_SELECTION_DISTANCE = MIN_SELECTION_DISTANCE *2f
    }
}
