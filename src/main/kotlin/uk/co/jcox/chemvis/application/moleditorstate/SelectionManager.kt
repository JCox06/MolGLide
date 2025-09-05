package uk.co.jcox.chemvis.application.moleditorstate

import org.joml.Vector3f
import org.joml.minus
import uk.co.jcox.chemvis.application.graph.ChemAtom
import uk.co.jcox.chemvis.application.graph.LevelContainer

class SelectionManager {

    var primarySelection: Type = Type.None

    fun update(levelContainer: LevelContainer, xPos: Float, yPos: Float) {
        primarySelection = findPrimarySelection(levelContainer, Vector3f(xPos, yPos, OrganicEditorState.ATOM_PLANE))
    }

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

    sealed class Type {
        object None: Type()
        data class Active(val atom: ChemAtom) : Type()
    }

    companion object {
        private const val MIN_SELECTION_DISTANCE = 30.0f
    }
}
