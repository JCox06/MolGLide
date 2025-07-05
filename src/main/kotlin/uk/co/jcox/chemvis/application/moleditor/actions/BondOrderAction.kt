package uk.co.jcox.chemvis.application.moleditor.actions

import org.apache.jena.sparql.pfunction.library.str
import org.checkerframework.checker.lock.qual.NewObject
import org.checkerframework.checker.units.qual.mol
import org.joml.Vector2f
import org.joml.Vector3f
import org.joml.minus
import org.joml.times
import uk.co.jcox.chemvis.application.chemengine.IMoleculeManager

import uk.co.jcox.chemvis.application.moleditor.LevelViewUtil
import uk.co.jcox.chemvis.application.moleditor.MolIDComponent
import uk.co.jcox.chemvis.application.moleditor.OrganicEditorState
import uk.co.jcox.chemvis.cvengine.scenegraph.EntityLevel
import uk.co.jcox.chemvis.cvengine.scenegraph.LineDrawerComponent
import uk.co.jcox.chemvis.cvengine.scenegraph.TransformComponent
import java.util.UUID

class BondOrderAction (
    val levelAtomA: EntityLevel,
    val levelAtomB: EntityLevel,
)
    : EditorAction() {


    override fun execute(molManager: IMoleculeManager, level: EntityLevel): UUID? {

        val levelParent = getParentMolecule(level)

        if (levelParent == null) {
            throw IllegalArgumentException("The two atoms are from different molecules, this action is not supported at the moment")
        }

        val structMolecule = levelParent.getComponent(MolIDComponent::class)
        val structAtomA = levelAtomA.getComponent(MolIDComponent::class)
        val structAtomB = levelAtomB.getComponent(MolIDComponent::class)

        val structBond = molManager.getJoiningBond(structMolecule.molID, structAtomA.molID, structAtomB.molID)

        if (structBond != null) {
            //There is already a bond between these two molecules, so make it a double bond
            increaseBondOrder(molManager, structMolecule.molID, structAtomA.molID, structAtomB.molID, structBond, levelParent)
        } else {
            //There is no bond between these molecules, so make a new bond and join them together
            formCyclisation(molManager, structMolecule.molID, structAtomA.molID, structAtomB.molID, levelParent)
        }

        molManager.recalculate(structMolecule.molID)

        removeImplicitHydrogenGroup(levelAtomA)
        removeImplicitHydrogenGroup(levelAtomB)

        val atomAImplicitH = molManager.getImplicitHydrogens(structAtomA.molID)
        val atomBImplicitH = molManager.getImplicitHydrogens(structAtomB.molID)

        if (!molManager.isOfElement(structAtomA.molID, "C")) {
            insertImplicitHydrogenGroup(levelAtomA, atomAImplicitH)

        }

        if (!molManager.isOfElement(structAtomB.molID, "C")) {
            insertImplicitHydrogenGroup(levelAtomB, atomBImplicitH)

        }
        return structMolecule.molID
    }


    private fun getParentMolecule(level: EntityLevel) : EntityLevel? {
        //Currently, this action is only supported if the atoms are from the same parent

        val atomAParentID = LevelViewUtil.getLvlMolFromLvlAtom(levelAtomA)
        val atomBParentID = LevelViewUtil.getLvlMolFromLvlAtom(levelAtomB)

        if (atomAParentID != atomBParentID) {
            return null
        }

        if (atomAParentID == null) {
            return null
        }

        val levelParent = level.findByID(atomAParentID)

        if (levelParent == null) {
            return null
        }

        return levelParent
    }


    private fun formCyclisation(molManager: IMoleculeManager, structMolecule: UUID, structAtomA: UUID, structAtomB: UUID, levelMolecule: EntityLevel, ) {
        val newBond = molManager.formBond(structMolecule, structAtomA, structAtomB, 1)

        val bond = LevelViewUtil.createBond(levelMolecule, levelAtomA, levelAtomB)
        LevelViewUtil.linkObject(newBond, bond)
    }


    private fun increaseBondOrder(molManager: IMoleculeManager, structMolecule: UUID, structAtomA: UUID, structAtomB: UUID, structBond: UUID, moleculeLevel: EntityLevel) {
        molManager.updateBondOrder(structMolecule, structBond, 2)

        //We need to add another bond that is just a small distance perpendicular to the bond direction

        //1) Get the bond direction
        val atomATrans = levelAtomA.getComponent(TransformComponent::class)
        val atomBTrans = levelAtomB.getComponent(TransformComponent::class)

        val atomALocalPos = Vector2f(atomATrans.x, atomATrans.y)
        val atomBLocalPos = Vector2f(atomBTrans.x, atomBTrans.y)

        val directionVec = atomALocalPos - atomBLocalPos
        val orthVec = Vector3f(directionVec.y, -directionVec.x, OrganicEditorState.XY_PLANE) * OrganicEditorState.DOUBLE_BOND_DISTANCE


        val shouldCentre = checkShouldCentre(molManager, structAtomA, structAtomB)


        insertLevelDoubleBond(moleculeLevel, orthVec, structBond, shouldCentre)

    }


    private fun checkShouldCentre(molManager: IMoleculeManager, structA: UUID, structB: UUID) : Boolean {
        //To check for carbonyl/imine groups either struct A is carbon and Struct B is oxygen/nitrogen

        val carbonylCheck = identifyBondMember(molManager, structA, structB, "C", "O")
        val imineCheck = identifyBondMember(molManager, structA, structB, "C", "N")

        return carbonylCheck || imineCheck
    }


    private fun identifyBondMember(molManager: IMoleculeManager, structA: UUID, structB: UUID, member1: String, member2: String) : Boolean {

        if (molManager.isOfElement(structA, member1) && molManager.isOfElement(structB, member2)) {
            return true
        }

        if (molManager.isOfElement(structA, member2) && molManager.isOfElement(structB, member1)) {
            return true
        }

        return false
    }

    private fun insertLevelDoubleBond(moleculeLevel: EntityLevel, orthVec: Vector3f, structBond: UUID, makeCentre: Boolean) {

        var offset = Vector3f()

        if (makeCentre) {
            offset = orthVec.div(-2.0f, Vector3f())
            offset.y -= 1.0f

            moleculeLevel.traverseFunc {
                if (it.hasComponent(LineDrawerComponent::class) && it.hasComponent(MolIDComponent::class)) {
                    val molComp = it.getComponent(MolIDComponent::class)
                    if (molComp.molID == structBond) {
                        if (it.hasComponent(TransformComponent::class)) {
                            val transComp = it.getComponent(TransformComponent::class)
                            transComp.x+= offset.x
                            transComp.y+= offset.y
                        }
                    }
                }
            }
        }

        val newBondEntity = moleculeLevel.addEntity()
        newBondEntity.addComponent(TransformComponent(orthVec.x + offset.x, orthVec.y + offset.y, -2.0f))
        newBondEntity.addComponent(LineDrawerComponent(levelAtomA.id, levelAtomB.id))
        LevelViewUtil.linkObject(structBond, newBondEntity)
    }
}