package uk.co.jcox.chemvis.application.moleditorstate.action

import org.checkerframework.checker.units.qual.mol
import org.joml.Math
import org.joml.Vector3f
import org.joml.minus
import org.joml.plus
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


        var currentNormal = Vector3f(1.0f, 0.0f, 0.0f)
        val firstAtomStruct = levelContainer.chemManager.addAtom(structNewMolecule, AtomInsert.CARBON)
        val firstAtom = ChemAtom(Vector3f(0.0f, -1.0f, 0.0f) * OrganicEditorState.CONNECTION_DISTANCE, firstAtomStruct, levelNewMolecule)
        firstAtom.visible = false
        levelNewMolecule.atoms.add(firstAtom)

        var angleAdvance = templateInsert.normalAdvance

        println(angleAdvance)

        var lastAtom = firstAtom
        var count = 0


        while (true) {

            val xPos = OrganicEditorState.CONNECTION_DISTANCE * Math.cos(Math.toRadians(angleAdvance))
            val yPos = OrganicEditorState.CONNECTION_DISTANCE * Math.sin(Math.toRadians(angleAdvance))
            val zPos = 0.0f

            val nextPos = Vector3f(xPos, yPos, zPos) + lastAtom.localPos

            if (nextPos.equals(firstAtom.localPos, 0.5f)) {

                val finalBondStruct = levelContainer.chemManager.formBond(structNewMolecule, firstAtomStruct, lastAtom.molManagerLink, BondOrder.SINGLE,
                    StereoChem.IN_PLANE)
                val finalBond = ChemBond(firstAtom, lastAtom, Vector3f(), finalBondStruct)

                levelNewMolecule.bonds.add(finalBond)

                break
            }

            val nextAtomStruct = levelContainer.chemManager.addAtom(structNewMolecule, AtomInsert.CARBON)
            val nextAtom = ChemAtom(nextPos, nextAtomStruct, levelNewMolecule)
            nextAtom.visible = false
            levelNewMolecule.atoms.add(nextAtom)

            val structBond = levelContainer.chemManager.formBond(
                structNewMolecule,
                lastAtom.molManagerLink,
                nextAtomStruct,
                BondOrder.SINGLE,
                StereoChem.IN_PLANE
            )
            val chemBond = ChemBond(lastAtom, nextAtom, Vector3f(), structBond)
            levelNewMolecule.bonds.add(chemBond)

            currentNormal.rotateAxis(templateInsert.normalAdvance, 1.0f, 1.0f, 0.0f)

            lastAtom = nextAtom

            count++

            angleAdvance += templateInsert.normalAdvance * 2

            //If something goes wrong exit
            if (count >= 100) {
                break
            }
        }

        levelContainer.chemManager.recalculate(levelNewMolecule.molManagerLink)

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

}