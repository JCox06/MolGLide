package uk.co.jcox.chemvis.application.moleditor.actions


import org.joml.Vector3f
import org.joml.minus
import uk.co.jcox.chemvis.application.chemengine.IMoleculeManager
import uk.co.jcox.chemvis.application.moleditor.AtomInsert
import uk.co.jcox.chemvis.application.moleditor.LevelMolLinkUtil
import uk.co.jcox.chemvis.application.moleditor.LevelViewUtil
import uk.co.jcox.chemvis.application.moleditor.MolIDComponent
import uk.co.jcox.chemvis.application.moleditor.NewOrganicEditorState
import uk.co.jcox.chemvis.cvengine.scenegraph.EntityLevel
import uk.co.jcox.chemvis.cvengine.scenegraph.TransformComponent
import java.util.UUID

class AtomInsertionAction (
    private val xPos: Float,
    private val yPos: Float,
    private val insert: AtomInsert,
    private val levelMolecule: EntityLevel,
    private val levelSelection: EntityLevel,
) : EditorAction() {

    var insertedAtom: UUID? = null

    override fun execute(molManager: IMoleculeManager, level: EntityLevel): UUID? {
        val levelMolPos = levelMolecule.getAbsolutePosition()
        val levelLocalMolPos = Vector3f(xPos, yPos, NewOrganicEditorState.XY_PLANE) - levelMolPos

        //1) Add the atom to the molecule struct side
        val structMolecule = levelMolecule.getComponent(MolIDComponent::class)
        val structNewAtom = molManager.addAtom(structMolecule.molID, insert.symbol)
        val structOldAtom = levelSelection.getComponent(MolIDComponent::class)


        //3) Form a bond between the two atoms
        val structBond = molManager.formBond(structMolecule.molID, structOldAtom.molID, structNewAtom, 1)

        molManager.recalculate(structMolecule.molID)


        //4) Start updating level side
        val levelNewAtom = LevelViewUtil.createLabel(levelMolecule, insert.symbol, levelLocalMolPos.x, levelLocalMolPos.y)

        LevelMolLinkUtil.linkObject(structNewAtom, levelNewAtom)
        val levelBond = LevelViewUtil.createBond(levelMolecule, levelSelection, levelNewAtom)
        LevelMolLinkUtil.linkObject(structBond, levelBond)

        LevelViewUtil.tagAsAtom(levelNewAtom)
        LevelViewUtil.linkParentLevel(levelNewAtom, levelMolecule)

        insertedAtom = levelNewAtom.id

        LevelViewUtil.removeAsExplicit(levelSelection)


        //5) Recalculate hydrogens
        molManager.recalculate(structMolecule.molID)

        replaceOldLabels(molManager, structMolecule.molID, structOldAtom.molID, levelSelection)

        //Add a label to the added object if required
        if (insert.hydrogenable && !molManager.isOfElement(structMolecule.molID, structNewAtom, "C")) {
            //Only add if it could have hydrogens and is not carbon
            addGhostGroup(molManager, levelNewAtom, structMolecule.molID, structNewAtom)
        }

        //Show implicit carbons on the level side
        determineToShowCarbons(molManager, structMolecule.molID, structOldAtom.molID, levelSelection)
        determineToShowCarbons(molManager, structMolecule.molID, structNewAtom, levelNewAtom)

        return structMolecule.molID
    }



    private fun determineToShowCarbons(molManager: IMoleculeManager, structMol: UUID, structAtom: UUID, levelAtom: EntityLevel) {
        if (! molManager.isOfElement(structMol, structAtom, "C")) {
            return
        }

        if (molManager.getBonds(structMol, structAtom) == NewOrganicEditorState.CARBON_IMPLICIT_LIMIT) {
            levelAtom.getComponent(TransformComponent::class).visible = false
        }
    }

}