package uk.co.jcox.chemvis.application.moleditor.actions

import uk.co.jcox.chemvis.application.chemengine.IMoleculeManager
import uk.co.jcox.chemvis.application.moleditor.GhostImplicitHydrogenGroupComponent
import uk.co.jcox.chemvis.application.moleditor.LevelViewUtil
import uk.co.jcox.chemvis.application.moleditor.NewOrganicEditorState
import uk.co.jcox.chemvis.cvengine.scenegraph.EntityLevel
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


    protected fun removeGhostGroups( levelNewAtom: EntityLevel) {
        val toRemove = mutableListOf<EntityLevel>()

        //First remove ghost groups
        levelNewAtom.getChildren().forEach {
            if (it.hasComponent(GhostImplicitHydrogenGroupComponent::class)) {
                toRemove.add(it)
            }
        }

        toRemove.forEach {
            levelNewAtom.removeEntity(it)
        }
    }

    protected fun refreshGhostGroups(molManager: IMoleculeManager, levelNewAtom: EntityLevel, structMolecule: UUID, structNewAtom: UUID) {

        removeGhostGroups(levelNewAtom)

        val getImplicitHydrogens = molManager.getImplicitHydrogens(structMolecule, structNewAtom)

        if (getImplicitHydrogens == 1) {
            val fakeH = LevelViewUtil.createLabel(levelNewAtom, "H", NewOrganicEditorState.INLINE_DIST, 0.0f)
            fakeH.addComponent(GhostImplicitHydrogenGroupComponent())
        } else {
            val fakeH = LevelViewUtil.createLabel(levelNewAtom, "H$getImplicitHydrogens", NewOrganicEditorState.INLINE_DIST, 0.0f)
            fakeH.addComponent(GhostImplicitHydrogenGroupComponent())
        }
    }

}


