package uk.co.jcox.chemvis.application.moleditor.actions

import uk.co.jcox.chemvis.application.chemengine.IMoleculeManager
import uk.co.jcox.chemvis.application.moleditor.AlwaysExplicit
import uk.co.jcox.chemvis.application.moleditor.AtomInsert
import uk.co.jcox.chemvis.application.moleditor.LevelViewUtil
import uk.co.jcox.chemvis.application.moleditor.MolIDComponent
import uk.co.jcox.chemvis.application.moleditor.NewOrganicEditorState
import uk.co.jcox.chemvis.cvengine.scenegraph.EntityLevel
import uk.co.jcox.chemvis.cvengine.scenegraph.TextComponent
import uk.co.jcox.chemvis.cvengine.scenegraph.TransformComponent
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


    private fun updateGhostGroups(molManager: IMoleculeManager, levelAtom: EntityLevel, structAtom: UUID) {

        removeImplicitHydrogenGroup(levelAtom)

        if (!levelAtom.hasComponent(AlwaysExplicit::class) && molManager.isOfElement(structAtom, "C")) {
            return
        }

        val newHydrogenCount = molManager.getImplicitHydrogens(structAtom)

        insertImplicitHydrogenGroup(levelAtom, newHydrogenCount)
    }


    private fun makeCarbonImplicit(molManager: IMoleculeManager, structMol: UUID, structCarbon: UUID, levelCarbon: EntityLevel) {
        //First check if carbon
        if (! molManager.isOfElement(structCarbon, "C")) {
            return
        }

        val bonds = molManager.getBonds(structMol, structCarbon)

        if (bonds >= NewOrganicEditorState.CARBON_IMPLICIT_LIMIT) {
            //Then hide the text component of the carbon
            val textComp = levelCarbon.getComponent(TransformComponent::class)
            textComp.visible = false
        }
    }
}