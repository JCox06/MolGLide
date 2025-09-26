package uk.co.jcox.chemvis.application.moleditorstate.action

import org.joml.Vector3f
import org.joml.times
import uk.co.jcox.chemvis.application.graph.ChemAtom
import uk.co.jcox.chemvis.application.graph.ChemBond
import uk.co.jcox.chemvis.application.graph.ChemMolecule
import uk.co.jcox.chemvis.application.graph.LevelContainer
import uk.co.jcox.chemvis.application.moleditorstate.AtomInsert
import uk.co.jcox.chemvis.application.moleditorstate.BondOrder
import uk.co.jcox.chemvis.application.moleditorstate.OrganicEditorState
import uk.co.jcox.chemvis.application.moleditorstate.StereoChem
import uk.co.jcox.chemvis.application.moleditorstate.TemplateRingInsert

class TemplateRingCreationAction (
    val clickX: Float,
    val clickY: Float,
    val templateInsert: TemplateRingInsert,
) : IAction {



    override fun execute(levelContainer: LevelContainer) {

        val structNewMolecule = levelContainer.chemManager.createMolecule()
        val levelNewMolecule = ChemMolecule(Vector3f(clickX, clickY, OrganicEditorState.ATOM_PLANE), structNewMolecule)
        levelContainer.sceneMolecules.add(levelNewMolecule)

        var lastItem: ChemAtom? = null
        for (seq in templateInsert.insertSequence) {
            //Make the atoms
            val newStructAtom = levelContainer.chemManager.addAtom(structNewMolecule, seq.insert)
            val newLevelAtom = ChemAtom(seq.pos * OrganicEditorState.CONNECTION_DISTANCE, newStructAtom, levelNewMolecule)
            levelNewMolecule.atoms.add(newLevelAtom)

            if (seq.insert == AtomInsert.CARBON) {
                newLevelAtom.visible = false
            }

            //Make the bonds
            lastItem?.let {
                formBondInSeq(levelContainer, levelNewMolecule, it, newLevelAtom, seq.startingOrder)
            }
            lastItem = newLevelAtom
        }
        //Form last bond in a ring
        val first = templateInsert.insertSequence.first()
        formBondInSeq(levelContainer, levelNewMolecule, levelNewMolecule.atoms.first(), levelNewMolecule.atoms.last(), first.startingOrder)


        levelContainer.chemManager.recalculate(levelNewMolecule.molManagerLink)
    }

    override fun undo(levelContainer: LevelContainer) {

    }


    private fun formBondInSeq(levelContainer: LevelContainer, levelNewMolecule: ChemMolecule, atom1: ChemAtom, atom2: ChemAtom, bondOrder: BondOrder) {
        val structBond = levelContainer.chemManager.formBond(levelNewMolecule.molManagerLink, atom1.molManagerLink, atom2.molManagerLink, bondOrder, StereoChem.IN_PLANE)
        val levelBond = ChemBond(atom1, atom2, Vector3f(), structBond)
        levelNewMolecule.bonds.add(levelBond)
    }
}