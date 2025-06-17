package uk.co.jcox.chemvis.application.moleditor.actions

import uk.co.jcox.chemvis.application.chemengine.IMoleculeManager
import uk.co.jcox.chemvis.application.moleditor.GhostImplicitHydrogenGroupComponent
import uk.co.jcox.chemvis.application.moleditor.LevelViewUtil
import uk.co.jcox.chemvis.application.moleditor.NewOrganicEditorState
import uk.co.jcox.chemvis.cvengine.scenegraph.EntityLevel
import uk.co.jcox.chemvis.cvengine.scenegraph.TextComponent
import java.util.UUID


abstract class EditorAction {


    /**
     * @return (optionally) the UUID of the molecule that has changed, to allow for internal recalculations (CDK Types)
     */
    protected abstract fun execute(molManager: IMoleculeManager, level: EntityLevel) : UUID?

    fun runAction(molManager: IMoleculeManager, level: EntityLevel) {
        val moleculeChanged = execute(molManager, level)
        if (moleculeChanged != null) {
            molManager.recalculate(moleculeChanged)
        }
    }


    protected fun addGhostGroup(molManager: IMoleculeManager, levelNewAtom: EntityLevel, structMolecule: UUID, structNewAtom: UUID) {
        val getImplicitHydrogens = molManager.getImplicitHydrogens(structMolecule, structNewAtom)

        if (getImplicitHydrogens == 1) {
            val fakeH = LevelViewUtil.createLabel(levelNewAtom, "H", NewOrganicEditorState.INLINE_DIST, 0.0f)
            fakeH.addComponent(GhostImplicitHydrogenGroupComponent())
        } else {
            val fakeH = LevelViewUtil.createLabel(levelNewAtom, "H$getImplicitHydrogens", NewOrganicEditorState.INLINE_DIST, 0.0f)
            fakeH.addComponent(GhostImplicitHydrogenGroupComponent())
        }
    }


    protected fun replaceOldLabels(molManager: IMoleculeManager, structMolecule: UUID, structOldAtom: UUID, levelSelection: EntityLevel) {
        //Remove old implicit labels and replace them
        //This works by changing the labels on the atom that was existing prior to this action be fired
        val toRemove = mutableListOf<EntityLevel>()

        for (entityLevel in levelSelection.getChildren()) {


            if (entityLevel.hasComponent(GhostImplicitHydrogenGroupComponent::class))  {

                //If a carbon gets a bond, remove all the implicit labels on it
                if (molManager.isOfElement(structMolecule, structOldAtom, "C")) {
                    toRemove.add(entityLevel)
                } else {
                    //Any element other than carbon should just have the labels updated to refelct the actual number of hydrogens
                    val textComp = entityLevel.getComponent(TextComponent::class)
                    val newInsertedImplicit = molManager.getImplicitHydrogens(structMolecule, structOldAtom)
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
    }

}


