package uk.co.jcox.chemvis.application.moleditor.actions

import org.joml.Vector2f
import org.joml.Vector3f
import org.joml.minus
import org.joml.times
import uk.co.jcox.chemvis.application.chemengine.IMoleculeManager
import uk.co.jcox.chemvis.application.moleditor.GhostImplicitHydrogenGroupComponent
import uk.co.jcox.chemvis.application.moleditor.MolIDComponent
import uk.co.jcox.chemvis.application.moleditor.NewOrganicEditorState
import uk.co.jcox.chemvis.cvengine.scenegraph.EntityLevel
import uk.co.jcox.chemvis.cvengine.scenegraph.LineDrawerComponent
import uk.co.jcox.chemvis.cvengine.scenegraph.TransformComponent
import java.util.UUID

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
        updateLevel(level)

        molManager.recalculate(structMolecule.molID)
        replaceOldLabels(molManager, structMolecule.molID, structAtomA.molID, levelAtomA)
        replaceOldLabels(molManager, structMolecule.molID, structAtomB.molID, levelAtomB)

        return structMolecule.molID
    }



    private fun updateStruct(molManager: IMoleculeManager, structMolecule: UUID, structBond: UUID) {
        molManager.updateBondOrder(structMolecule, structBond, 2)
    }

    private fun updateLevel(level: EntityLevel) {
        //We need to add another bond that is just a small distance perpendicular to the bond direction

        //1) Get the bond direction
        val atomATrans = levelAtomA.getComponent(TransformComponent::class)
        val atomBTrans = levelAtomB.getComponent(TransformComponent::class)

        val atomALocalPos = Vector2f(atomATrans.x, atomATrans.y)
        val atomBLocalPos = Vector2f(atomBTrans.x, atomBTrans.y)

        val directionVec = atomALocalPos - atomBLocalPos
        val orthVec = Vector3f(directionVec.y, -directionVec.x, NewOrganicEditorState.XY_PLANE) * NewOrganicEditorState.DOUBLE_BOND_DISTANCE

        val newBondEntity = level.addEntity()
        newBondEntity.addComponent(TransformComponent(orthVec.x, orthVec.y, orthVec.z))
        newBondEntity.addComponent(LineDrawerComponent(levelAtomA.id, levelAtomB.id))
    }
}