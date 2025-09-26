package uk.co.jcox.chemvis.application.moleditorstate.action

import org.checkerframework.checker.units.qual.mol
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

    var createdMolecule: ChemMolecule? = null


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

        createdMolecule = levelNewMolecule
    }

    override fun undo(levelContainer: LevelContainer) {
        val moleculeToDelete = createdMolecule ?: return

        moleculeToDelete.bonds.forEach { bond ->
            levelContainer.chemManager.deleteBond(moleculeToDelete.molManagerLink, bond.molManagerLink)
        }

        moleculeToDelete.atoms.forEach { atom ->
            levelContainer.chemManager.deleteAtom(moleculeToDelete.molManagerLink, atom.molManagerLink)
        }

        levelContainer.chemManager.deleteMolecule(moleculeToDelete.molManagerLink)

        levelContainer.sceneMolecules.remove(moleculeToDelete)

        createdMolecule = null
    }


    private fun formBondInSeq(levelContainer: LevelContainer, levelNewMolecule: ChemMolecule, atom1: ChemAtom, atom2: ChemAtom, bondOrder: BondOrder) {
        val structBond = levelContainer.chemManager.formBond(levelNewMolecule.molManagerLink, atom1.molManagerLink, atom2.molManagerLink, bondOrder, StereoChem.IN_PLANE)
        val levelBond = ChemBond(atom1, atom2, Vector3f(), structBond)
        levelNewMolecule.bonds.add(levelBond)
    }
}