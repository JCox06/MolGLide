package uk.co.jcox.chemvis.application.moleditorstate.action

import org.checkerframework.checker.units.qual.mol
import uk.co.jcox.chemvis.application.data.DataBond
import uk.co.jcox.chemvis.application.graph.ChemAtom
import uk.co.jcox.chemvis.application.graph.ChemBond
import uk.co.jcox.chemvis.application.graph.ChemMolecule
import uk.co.jcox.chemvis.application.graph.LevelContainer

class DirectBondCreationAction(
    private val dataBond: DataBond,
    private val molecule: ChemMolecule,
    private val atomA: ChemAtom,
    private val atomB: ChemAtom,
) : IAction {


    private lateinit var bond: ChemBond

    override fun execute(levelContainer: LevelContainer) {
        bond = molecule.formBasicConnection(atomA, atomB)
        bond.bondOffset = dataBond.offset
        bond.centredBond = dataBond.centred
        bond.flipDoubleBond = dataBond.bondFlip
        bond.bisectorNudge = dataBond.nudge
        bond.setStereo(dataBond.stereo)
        molecule.updateBondOrder(bond, dataBond.order)
        molecule.setAromaticity(bond, dataBond.aromatic)
    }

    override fun undo(levelContainer: LevelContainer) {
        molecule.removeBond(bond)
    }
}