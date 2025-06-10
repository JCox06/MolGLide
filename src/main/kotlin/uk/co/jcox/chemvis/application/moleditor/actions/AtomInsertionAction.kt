package uk.co.jcox.chemvis.application.moleditor.actions


import org.checkerframework.checker.units.qual.mol
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
import uk.co.jcox.chemvis.cvengine.scenegraph.TextComponent
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


        //3) Form a bond between the two atoms
        val structBond = molManager.formBond(structMolecule.molID, structOldAtom.molID, structNewAtom, 1)

        molManager.recalculate(structMolecule.molID)


        //4) Start updating level side
        val levelNewAtom = LevelViewUtil.createLabel(levelMolecule, insert.symbol, levelLocalMolPos.x, levelLocalMolPos.y)

        LevelMolLinkUtil.linkObject(structNewAtom, levelNewAtom)
        val levelBond = LevelViewUtil.createBond(levelMolecule, levelSelection, levelNewAtom)
        LevelMolLinkUtil.linkObject(structBond, levelBond)

        LevelViewUtil.tagAsAtom(levelNewAtom)

        insertedAtom = levelNewAtom.id

        LevelViewUtil.removeAsExplicit(levelSelection)


        //5) Recalculate hydrogens
        if (insertImplicitHydrogens && insert.hydrogenable) {
            molManager.recalculate(structMolecule.molID)
        }

        //Remove old implicit labels and replace them
        val toRemove = mutableListOf<EntityLevel>()
        for (entityLevel in levelSelection.getChildren()) {
            if (entityLevel.hasComponent(GhostImplicitHydrogenGroupComponent::class))  {
                if (molManager.isOfElement(structMolecule.molID, structOldAtom.molID, "C")) {
                    toRemove.add(entityLevel)
                } else {
                    val textComp = entityLevel.getComponent(TextComponent::class)
                    val newInsertedImplicit = molManager.getImplicitHydrogens(structMolecule.molID, structOldAtom.molID)
                    if (newInsertedImplicit <= 0) {
                        toRemove.add(entityLevel)
                    }
                    else if (newInsertedImplicit == 1) {
                        textComp.text = "H"
                    } else {
                        textComp.text = "H$newInsertedImplicit"
                    }

                }
            }
        }
        toRemove.forEach {
            levelSelection.removeEntity(it)
        }

        //Add a label to the added object if required
        if (insert.hydrogenable && !molManager.isOfElement(structMolecule.molID, structNewAtom, "C")) {
            //Only add if it could have hydrogens and is not carbon
            val getImplicitHydrogens = molManager.getImplicitHydrogens(structMolecule.molID, structNewAtom)

            if (getImplicitHydrogens == 1) {
                val fakeH = LevelViewUtil.createLabel(levelNewAtom, "H", NewOrganicEditorState.INLINE_DIST, 0.0f)
                fakeH.addComponent(GhostImplicitHydrogenGroupComponent())
            } else {
                val fakeH = LevelViewUtil.createLabel(levelNewAtom, "H$getImplicitHydrogens", NewOrganicEditorState.INLINE_DIST, 0.0f)
                fakeH.addComponent(GhostImplicitHydrogenGroupComponent())
            }
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