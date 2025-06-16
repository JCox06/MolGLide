package uk.co.jcox.chemvis.application.moleditor.actions

import org.checkerframework.checker.units.qual.mol
import uk.co.jcox.chemvis.application.chemengine.IMoleculeManager
import uk.co.jcox.chemvis.application.moleditor.MolIDComponent
import uk.co.jcox.chemvis.cvengine.scenegraph.EntityLevel
import uk.co.jcox.chemvis.cvengine.scenegraph.TransformComponent
import java.util.UUID
import javax.swing.text.html.parser.Entity

class BondOrderAction (
    val levelMolecule: EntityLevel,
    val levelAtomA: EntityLevel,
    val levelAtomB: EntityLevel,
)
    : EditorAction() {


    override fun execute(molManager: IMoleculeManager, level: EntityLevel): UUID? {
        val structMolecule = levelMolecule.getComponent(MolIDComponent::class)
        val structAtomA = levelAtomA.getComponent(MolIDComponent::class)
        val structAtomB = levelAtomB.getComponent(MolIDComponent::class)

        val structBond = molManager.getJoiningBond(structMolecule.molID, structAtomA.molID, structAtomB.molID)


        updateStruct(molManager, structMolecule.molID, structBond)

        return structMolecule.molID
    }



    private fun updateStruct(molManager: IMoleculeManager, structMolecule: UUID, structBond: UUID) {
        molManager.updateBondOrder(structMolecule, structBond, 2)
    }
}