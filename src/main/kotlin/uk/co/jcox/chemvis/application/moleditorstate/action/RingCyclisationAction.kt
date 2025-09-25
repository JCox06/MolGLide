package uk.co.jcox.chemvis.application.moleditorstate.action

import org.joml.Vector3f
import uk.co.jcox.chemvis.application.moleditorstate.BondOrder
import uk.co.jcox.chemvis.application.graph.ChemAtom
import uk.co.jcox.chemvis.application.graph.ChemBond
import uk.co.jcox.chemvis.application.graph.ChemMolecule
import uk.co.jcox.chemvis.application.graph.LevelContainer
import uk.co.jcox.chemvis.application.moleditorstate.StereoChem


class RingCyclisationAction(
    private val molecule: ChemMolecule,
    private val atomA: ChemAtom,
    private val atomB: ChemAtom,
) : IAction {

    private var bond: ChemBond? = null

    override fun execute(levelContainer: LevelContainer) {
        //Create a new bond struct side
        val bondID = levelContainer.chemManager.formBond(molecule.molManagerLink, atomA.molManagerLink, atomB.molManagerLink, BondOrder.SINGLE, StereoChem.IN_PLANE)

        //Create new bond on the level side
        val levelBond = ChemBond(atomA, atomB, Vector3f(), bondID)
        molecule.bonds.add(levelBond)
        this.bond = levelBond

        levelContainer.chemManager.recalculate(molecule.molManagerLink)    }

    override fun undo(levelContainer: LevelContainer) {

        bond?.let {
            this.molecule.bonds.remove(it)
            levelContainer.chemManager.deleteBond(molecule.molManagerLink, it.molManagerLink)
        }

        levelContainer.chemManager.recalculate(molecule.molManagerLink)    }

}