package uk.co.jcox.chemvis.application.moleditorstate.action

import org.checkerframework.checker.units.qual.mol
import org.joml.Vector3f
import org.openscience.cdk.isomorphism.TransformOp
import uk.co.jcox.chemvis.application.graph.ChemAtom
import uk.co.jcox.chemvis.application.graph.ChemBond
import uk.co.jcox.chemvis.application.graph.ChemMolecule
import uk.co.jcox.chemvis.application.graph.LevelContainer


class RingCyclisationAction(
    private val molecule: ChemMolecule,
    private val atomA: ChemAtom,
    private val atomB: ChemAtom,
) : IAction {

    private var bond: ChemBond? = null

    override fun execute(levelContainer: LevelContainer) {
        bond = molecule.formBasicConnection(atomA, atomB)
    }

    override fun undo(levelContainer: LevelContainer) {
        bond?.let {
            molecule.removeBond(it)
        }
    }

}