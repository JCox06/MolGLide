package uk.co.jcox.chemvis.application.moleditor.actions


import nu.xom.Text
import uk.co.jcox.chemvis.application.MolGLide
import uk.co.jcox.chemvis.application.chemengine.IMoleculeManager
import uk.co.jcox.chemvis.application.moleditor.AlwaysExplicit
import uk.co.jcox.chemvis.application.moleditor.AtomComponent
import uk.co.jcox.chemvis.application.moleditor.GhostImplicitHydrogenGroupComponent
import uk.co.jcox.chemvis.application.moleditor.LevelParentComponent
import uk.co.jcox.chemvis.application.moleditor.LevelViewUtil
import uk.co.jcox.chemvis.application.moleditor.MolIDComponent
import uk.co.jcox.chemvis.application.moleditor.NewOrganicEditorState
import uk.co.jcox.chemvis.cvengine.scenegraph.EntityLevel
import uk.co.jcox.chemvis.cvengine.scenegraph.LineDrawerComponent
import uk.co.jcox.chemvis.cvengine.scenegraph.TextComponent
import uk.co.jcox.chemvis.cvengine.scenegraph.TransformComponent
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

    protected fun insertImplicitHydrogenGroup(levelAtom: EntityLevel, hydrogenCount: Int) {
        if (hydrogenCount == 0) {
            return
        }

        var label = "H"
        if (hydrogenCount > 1) {
            label = "H$hydrogenCount"
        }

        val fakeLabel = LevelViewUtil.createLabel(levelAtom, label, NewOrganicEditorState.INLINE_DIST, 0.0f)
        fakeLabel.addComponent(GhostImplicitHydrogenGroupComponent())
    }

}


