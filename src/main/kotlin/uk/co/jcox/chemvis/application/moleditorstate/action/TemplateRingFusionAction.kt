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
import kotlin.math.atan2

class TemplateRingFusionAction (
    val commonBond: ChemBond,
    val templateInsert: TemplateRingInsert,
) : IAction {


    override fun execute(levelContainer: LevelContainer) {

        val atomA = commonBond.atomA
        val atomB = commonBond.atomB
        val molecule = atomA.parent

        var currentNormal = (atomA.localPos - atomB.localPos).normalize()

        val tanValue = currentNormal.angle(Vector3f(0.0f, 0.0f, 1.0f))
        val angle = 0f

        var angleAdvance = -templateInsert.normalAdvance + angle


        println(angle)

        var lastAtom = atomA
        var count = 0


        while (true) {

            val xPos = OrganicEditorState.CONNECTION_DISTANCE * Math.cos(Math.toRadians(angleAdvance))
            val yPos = OrganicEditorState.CONNECTION_DISTANCE * Math.sin(Math.toRadians(angleAdvance))
            val zPos = 0.0f

            val nextPos = Vector3f(xPos, yPos, zPos) + lastAtom.localPos

            if (nextPos.equals(atomB.localPos, 0.5f)) {

                val finalBondStruct = levelContainer.chemManager.formBond(molecule.molManagerLink, lastAtom.molManagerLink, atomB.molManagerLink, BondOrder.SINGLE,
                    StereoChem.IN_PLANE)
                val finalBond = ChemBond(lastAtom, atomB, Vector3f(), finalBondStruct)

                molecule.bonds.add(finalBond)

                break
            }

            val nextAtomStruct = levelContainer.chemManager.addAtom(molecule.molManagerLink, AtomInsert.CARBON)
            val nextAtom = ChemAtom(nextPos, nextAtomStruct, molecule)
            nextAtom.visible = false
            molecule.atoms.add(nextAtom)

            val structBond = levelContainer.chemManager.formBond(
                molecule.molManagerLink,
                lastAtom.molManagerLink,
                nextAtomStruct,
                BondOrder.SINGLE,
                StereoChem.IN_PLANE
            )
            val chemBond = ChemBond(lastAtom, nextAtom, Vector3f(), structBond)
            molecule.bonds.add(chemBond)

            lastAtom = nextAtom

            count++

            angleAdvance += templateInsert.normalAdvance * 2

            //If something goes wrong exit
            if (count >= 100) {
                break
            }
        }

        levelContainer.chemManager.recalculate(molecule.molManagerLink)

    }

    override fun undo(levelContainer: LevelContainer) {

    }

}