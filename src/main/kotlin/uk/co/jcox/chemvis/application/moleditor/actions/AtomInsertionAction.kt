package uk.co.jcox.chemvis.application.moleditor.actions


import org.joml.Vector3f
import org.joml.minus
import uk.co.jcox.chemvis.application.chemengine.IMoleculeManager
import uk.co.jcox.chemvis.application.moleditor.AtomInsert
import uk.co.jcox.chemvis.application.moleditor.GhostImplicitHydrogenGroupComponent
import uk.co.jcox.chemvis.application.moleditor.LevelMolLinkUtil
import uk.co.jcox.chemvis.application.moleditor.LevelViewUtil
import uk.co.jcox.chemvis.application.moleditor.MolIDComponent
import uk.co.jcox.chemvis.application.moleditor.NewOrganicEditorState
import uk.co.jcox.chemvis.cvengine.scenegraph.EntityLevel
import uk.co.jcox.chemvis.cvengine.scenegraph.TransformComponent
import java.util.UUID
import kotlin.enums.enumEntries

class AtomInsertionAction (
    private val xPos: Float,
    private val yPos: Float,
    private val insert: AtomInsert,
    private val levelMolecule: EntityLevel,
    private val levelSelection: EntityLevel,
    private val insertImplicitHydrogens: Boolean,
) : EditorAction() {

    var insertedAtom: UUID? = null

    override fun execute(molManager: IMoleculeManager, level: EntityLevel): UUID? {
        val levelMolPos = levelMolecule.getAbsolutePosition()
        val levelLocalMolPos = Vector3f(xPos, yPos, NewOrganicEditorState.Companion.XY_PLANE) - levelMolPos

        //1) Add the atom to the molecule struct side
        val structMolecule = levelMolecule.getComponent(MolIDComponent::class)
        val structNewAtom = molManager.addAtom(structMolecule.molID, insert.symbol)
        val structOldAtom = levelSelection.getComponent(MolIDComponent::class)

        //2) Remove one implicit hydrogen
//        molManager.removeImplicitHydrogenIfPossible(structMolecule.molID, structOldAtom.molID)

        //3) Form a bond between the two atoms
        val structBond = molManager.formBond(structMolecule.molID, structOldAtom.molID, structNewAtom, 1)

        molManager.recalculate(structMolecule.molID)

        println("ON OLD: ${molManager.getImplicitHydrogens(structMolecule.molID, structOldAtom.molID)}")
        println("ON NEW: ${molManager.getImplicitHydrogens(structMolecule.molID, structNewAtom)}")
        println("Bonds on OLD: ${molManager.getBonds(structMolecule.molID, structOldAtom.molID)}")
        println("Bonds on NEW: ${molManager.getBonds(structMolecule.molID, structNewAtom)}")


        //4) Start updating level side
        val levelNewAtom = LevelViewUtil.createLabel(levelMolecule, insert.symbol, levelLocalMolPos.x, levelLocalMolPos.y)
        LevelViewUtil.createSelectionMarker(levelNewAtom)

        LevelViewUtil.createInlinePlaceholder(levelNewAtom, NewOrganicEditorState.INLINE_DIST, 0.0f, 0.0f)
        LevelViewUtil.createInlinePlaceholder(levelNewAtom, -NewOrganicEditorState.INLINE_DIST, 0.0f, 0.0f)
        LevelViewUtil.createInlinePlaceholder(levelNewAtom, 0.0f , NewOrganicEditorState.INLINE_DIST, 0.0f)
        LevelViewUtil.createInlinePlaceholder(levelNewAtom, 0.0f , -NewOrganicEditorState.INLINE_DIST, 0.0f)

        LevelMolLinkUtil.linkObject(structNewAtom, levelNewAtom)
        val levelBond = LevelViewUtil.createBond(levelMolecule, levelSelection, levelNewAtom)
        LevelMolLinkUtil.linkObject(structBond, levelBond)

        LevelViewUtil.tagAsAtom(levelNewAtom)

        insertedAtom = levelNewAtom.id

        LevelViewUtil.removeAsExplicit(levelSelection)


        //Remove old implicit labels
        for (entityLevel in levelSelection.getChildren()) {
            if (entityLevel.hasComponent(GhostImplicitHydrogenGroupComponent::class))  {
                entityLevel.getComponent(TransformComponent::class).visible = false
            }
        }

        //5) Add and remove implicit hydrogens
        if (insertImplicitHydrogens && insert.hydrogenable) {
            molManager.recalculate(structMolecule.molID)
        }

        return structMolecule.molID
    }

}