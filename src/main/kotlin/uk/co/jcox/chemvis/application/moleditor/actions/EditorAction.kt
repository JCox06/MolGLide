package uk.co.jcox.chemvis.application.moleditor.actions


import uk.co.jcox.chemvis.application.chemengine.IMoleculeManager
import uk.co.jcox.chemvis.application.moleditor.AlwaysExplicit
import uk.co.jcox.chemvis.application.moleditor.GhostImplicitHydrogenGroupComponent
import uk.co.jcox.chemvis.application.moleditor.LevelViewUtil
import uk.co.jcox.chemvis.application.moleditor.OrganicEditorState
import uk.co.jcox.chemvis.cvengine.scenegraph.EntityLevel
import uk.co.jcox.chemvis.cvengine.scenegraph.TransformComponent
import java.util.UUID


/**
 * An editor action represents a small event to the workstate (main EntityLevel + IMoleculeManager)
 * Each action is meant to run to completion
 */
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


    /**
     * If given an atom from the level, this method will delete all child groups with the GhostImplicitHydrogenGroupComponent class
     * @param levelAtom the level component to perform this action
     *
     */
    protected fun removeImplicitHydrogenGroup(levelAtom: EntityLevel) {

        val toRemove = mutableListOf<EntityLevel>()

        levelAtom.getChildren().forEach {
            if (it.hasComponent(GhostImplicitHydrogenGroupComponent::class)) {
                toRemove.add(it)
            }
        }

        toRemove.forEach {
            levelAtom.removeEntity(it)
        }
    }

    /**
     * This method will add a hydrogen implicit group given the number of hydrogens to add
     * @param levelAtom The atom to perfom this action on
     * @param hydrogenCount the number of hydrogen atoms to count
     */
    protected fun insertImplicitHydrogenGroup(levelAtom: EntityLevel, hydrogenCount: Int) {
        if (hydrogenCount == 0) {
            return
        }

        var label = "H"
        if (hydrogenCount > 1) {
            label = "H$hydrogenCount"
        }

        val fakeLabel = LevelViewUtil.createLabel(levelAtom, label, OrganicEditorState.INLINE_DIST, 0.0f)
        fakeLabel.addComponent(GhostImplicitHydrogenGroupComponent())
    }


    protected fun updateGhostGroups(molManager: IMoleculeManager, levelAtom: EntityLevel, structAtom: UUID) {

        removeImplicitHydrogenGroup(levelAtom)

        if (!levelAtom.hasComponent(AlwaysExplicit::class) && molManager.isOfElement(structAtom, "C")) {
            return
        }

        val newHydrogenCount = molManager.getImplicitHydrogens(structAtom)

        insertImplicitHydrogenGroup(levelAtom, newHydrogenCount)
    }


    protected fun makeCarbonImplicit(molManager: IMoleculeManager, structMol: UUID, structCarbon: UUID, levelCarbon: EntityLevel) {
        //First check if carbon
        if (! molManager.isOfElement(structCarbon, "C")) {
            return
        }

        val bonds = molManager.getBonds(structMol, structCarbon)

        if (bonds >= OrganicEditorState.CARBON_IMPLICIT_LIMIT) {
            //Then hide the text component of the carbon
            val textComp = levelCarbon.getComponent(TransformComponent::class)
            textComp.visible = false
        }
    }
}


