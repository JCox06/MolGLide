package uk.co.jcox.chemvis.application.moleditorstate.action

import org.checkerframework.checker.units.qual.mol
import org.joml.Vector3f
import org.joml.minus
import org.joml.times
import uk.co.jcox.chemvis.application.graph.ChemBond
import uk.co.jcox.chemvis.application.graph.ChemMolecule
import uk.co.jcox.chemvis.application.graph.LevelContainer
import uk.co.jcox.chemvis.application.moleditorstate.OrganicEditorState

class BondOrderChangeAction (val molecule: ChemMolecule, val bond: ChemBond) : IAction {


    override fun execute(levelContainer: LevelContainer) {
        levelContainer.structMolecules.updateBondOrder(molecule.molManagerLink, bond.molManagerLink, 2)
        bond.type = ChemBond.Type.DOUBLE

        levelContainer.structMolecules.recalculate(molecule.molManagerLink)

    }

    override fun undo(levelContainer: LevelContainer) {
        levelContainer.structMolecules.updateBondOrder(molecule.molManagerLink, bond.molManagerLink, 1)
        bond.type = ChemBond.Type.SINGLE

        levelContainer.structMolecules.recalculate(molecule.molManagerLink)
    }
}