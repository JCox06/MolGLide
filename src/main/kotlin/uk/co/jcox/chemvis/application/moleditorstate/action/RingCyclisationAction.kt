package uk.co.jcox.chemvis.application.moleditorstate.action

import org.joml.Vector3f
import uk.co.jcox.chemvis.application.graph.ChemAtom
import uk.co.jcox.chemvis.application.graph.ChemBond
import uk.co.jcox.chemvis.application.graph.ChemMolecule
import uk.co.jcox.chemvis.application.graph.LevelContainer
import java.util.UUID


class RingCyclisationAction(
    private val molecule: ChemMolecule,
    private val atomA: ChemAtom,
    private val atomB: ChemAtom,
) : IAction {

    private var bond: ChemBond? = null

    override fun execute(levelContainer: LevelContainer) {
        //Create a new bond struct side
        val bondID = levelContainer.structMolecules.formBond(molecule.molManagerLink, atomA.molManagerLink, atomB.molManagerLink, 1)

        //Create new bond on the level side
        val levelBond = ChemBond(atomA, atomB, ChemBond.Type.SINGLE, Vector3f(), bondID)
        molecule.bonds.add(levelBond)
        this.bond = levelBond

        updateImplicitHydrogens(levelContainer)
    }

    override fun undo(levelContainer: LevelContainer) {

        bond?.let {
            this.molecule.bonds.remove(it)
            levelContainer.structMolecules.deleteBond(molecule.molManagerLink, it.molManagerLink)
        }

        updateImplicitHydrogens(levelContainer)
    }

    private fun updateImplicitHydrogens(levelContainer: LevelContainer) {
        levelContainer.structMolecules.recalculate(molecule.molManagerLink)
        val atomAH = levelContainer.structMolecules.getImplicitHydrogens(atomA.molManagerLink)
        val atomBH = levelContainer.structMolecules.getImplicitHydrogens(atomB.molManagerLink)
        atomA.implicitHydrogenCount = atomAH
        atomB.implicitHydrogenCount = atomBH
    }
}