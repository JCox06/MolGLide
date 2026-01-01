package uk.co.jcox.chemvis.application.moleditorstate.action

import uk.co.jcox.chemvis.application.graph.ChemMolecule
import uk.co.jcox.chemvis.application.graph.LevelContainer
import uk.co.jcox.chemvis.application.moleditorstate.AtomInsert
import java.util.*

class AtomCreationAction (
    private val newAtomX: Float,
    private val newAtomY: Float,
    private val insert: AtomInsert,
) : IAction {

    /**
     * Hold a reference to the atom that was inserted
     * That way, we can easily retrieve it should #undo is called
     */
    private var levelMolecule: ChemMolecule? = null

    override fun execute(levelContainer: LevelContainer) {
        //Create the molecule
        val chemMolecule: ChemMolecule = ChemMolecule()
        chemMolecule.positionOffset.x = newAtomX
        chemMolecule.positionOffset.y = newAtomY

        //Add the atom
        chemMolecule.addAtom(insert, 0.0f, 0.0f)

        //Add the molecule to the level
        levelContainer.sceneMolecules.add(chemMolecule)

        levelMolecule = chemMolecule
    }


    override fun undo(levelContainer: LevelContainer) {
        levelMolecule?.let { levelContainer.sceneMolecules.remove(it) }
    }

    override fun redo(levelContainer: LevelContainer) {
        levelMolecule?.let { levelContainer.sceneMolecules.add(it) }
    }
}