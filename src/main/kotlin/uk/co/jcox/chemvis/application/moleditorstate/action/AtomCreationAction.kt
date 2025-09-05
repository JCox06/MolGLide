package uk.co.jcox.chemvis.application.moleditorstate.action

import org.joml.Vector3f
import uk.co.jcox.chemvis.application.graph.ChemAtom
import uk.co.jcox.chemvis.application.graph.ChemMolecule
import uk.co.jcox.chemvis.application.graph.LevelContainer
import uk.co.jcox.chemvis.application.moleditorstate.AtomInsert
import uk.co.jcox.chemvis.application.moleditorstate.OrganicEditorState
import java.util.UUID

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
    private var structMolecule: UUID? = null
    private var structAtom: UUID? = null



    override fun execute(levelContainer: LevelContainer) {

        //Create the chem data
        val structMolecule = levelContainer.structMolecules.createMolecule()
        val structAtom = levelContainer.structMolecules.addAtom(structMolecule, insert.symbol)

        //Create the level data
        val levelMolecule = ChemMolecule(Vector3f(newAtomX, newAtomY, OrganicEditorState.ATOM_PLANE), structMolecule)
        val levelAtom = ChemAtom(Vector3f(0.0f, 0.0f, 0.0f), structAtom, insert.symbol, levelMolecule)
        levelMolecule.atoms.add(levelAtom)

        levelContainer.sceneMolecules.add(levelMolecule)

        this.levelMolecule = levelMolecule
        this.structMolecule = structMolecule
        this.structAtom = structAtom
    }


    override fun undo(levelContainer: LevelContainer) {
        //TO undo this action, we must delete what was added
        //Use the references (the properties) at the top of this file

        //First remove the level molecule
        levelContainer.sceneMolecules.remove(this.levelMolecule)

        //And now remove the structs
        this.structAtom?.let {
            levelContainer.structMolecules.deleteAtom(it)
        }
        this.structMolecule?.let {
            levelContainer.structMolecules.deleteMolecule(it)

        }
    }
}