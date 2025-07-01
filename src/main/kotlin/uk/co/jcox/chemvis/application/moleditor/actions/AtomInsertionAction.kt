package uk.co.jcox.chemvis.application.moleditor.actions

import uk.co.jcox.chemvis.application.chemengine.IMoleculeManager
import uk.co.jcox.chemvis.application.moleditor.AtomComponent
import uk.co.jcox.chemvis.application.moleditor.AtomInsert
import uk.co.jcox.chemvis.application.moleditor.LevelViewUtil
import uk.co.jcox.chemvis.application.moleditor.MolIDComponent
import uk.co.jcox.chemvis.cvengine.scenegraph.EntityLevel
import java.util.UUID

class AtomInsertionAction (
    val insert: AtomInsert,
    val levelOldAtom: EntityLevel
) : EditorAction(){


    var insertion: EntityLevel? = null

    override fun execute(molManager: IMoleculeManager, level: EntityLevel): UUID? {

        val levelMolID = LevelViewUtil.getLvlMolFromLvlAtom(levelOldAtom)
        if (levelMolID == null) {
            return null
        }

        val levelMol = level.findByID(levelMolID)

        //Struct side objects:
        val structOldAtom = levelOldAtom.getComponent(MolIDComponent::class)
        val structMol = levelMol?.getComponent(MolIDComponent::class)

        if (structOldAtom == null || structMol == null) {
            return null
        }

        LevelViewUtil.removeAsExplicit(levelOldAtom)

        val levelNewAtom = addInsertedAtom(levelMol, molManager, structMol.molID)
        addBond(molManager, levelMol, levelOldAtom, levelNewAtom.first, structMol.molID, structOldAtom.molID, levelNewAtom.second)

        molManager.recalculate(structMol.molID)
        updateGhostGroups(molManager, levelNewAtom.first, levelNewAtom.second)
        updateGhostGroups(molManager, levelOldAtom, structOldAtom.molID)

        makeCarbonImplicit(molManager, structMol.molID, structOldAtom.molID, levelOldAtom)
        makeCarbonImplicit(molManager, structMol.molID, levelNewAtom.second, levelNewAtom.first)

        joinConnectedAtoms(levelOldAtom, levelNewAtom.first)

        return structMol.molID
    }


    private fun addInsertedAtom(levelMol: EntityLevel, molMan: IMoleculeManager, structMol: UUID) : Pair<EntityLevel, UUID> {
        val structNewAtom = molMan.addAtom(structMol, insert.symbol)
        //The position will not matter as it is updated anyway after this event
        val levelNewAtom = LevelViewUtil.createLabel(levelMol, insert.symbol, 0.0f, 0.0f, 1.0f)
        LevelViewUtil.tagAsAtom(levelNewAtom)
        LevelViewUtil.linkParentLevel(levelNewAtom, levelMol)
        LevelViewUtil.linkObject(structNewAtom, levelNewAtom)

        insertion = levelNewAtom
        return Pair(levelNewAtom, structNewAtom)
    }


    private fun addBond(molMan: IMoleculeManager, levelMol: EntityLevel, levelAtomA: EntityLevel, levelAtomB: EntityLevel, structMol: UUID,  structAtomA: UUID, structAtomB: UUID) {

        val structBond = molMan.formBond(structMol, structAtomA, structAtomB, 1)

        val levelBond = LevelViewUtil.createBond(levelMol, levelAtomA, levelAtomB)

        LevelViewUtil.linkObject(structBond, levelBond)
    }


    private fun joinConnectedAtoms(oldAtom: EntityLevel, newAtom: EntityLevel) {
        val oldAtomComp = oldAtom.getComponent(AtomComponent::class)
        val newAtomComp = newAtom.getComponent(AtomComponent::class)

        newAtomComp.connectedEntities.add(oldAtom.id)
        oldAtomComp.connectedEntities.add(newAtom.id)
    }
}