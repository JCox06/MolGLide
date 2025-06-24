package uk.co.jcox.chemvis.application.moleditor


import org.joml.minus
import uk.co.jcox.chemvis.application.MolGLide
import uk.co.jcox.chemvis.cvengine.scenegraph.EntityLevel
import uk.co.jcox.chemvis.cvengine.scenegraph.LineDrawerComponent
import uk.co.jcox.chemvis.cvengine.scenegraph.ObjComponent
import uk.co.jcox.chemvis.cvengine.scenegraph.TextComponent
import uk.co.jcox.chemvis.cvengine.scenegraph.TransformComponent
import java.util.UUID

object LevelViewUtil {

    /**
     * Creates a label level side, it doesn't have to be an atom, but usually is
     * @param molecule represents the molecule level side that the atom should be created into
     * @param insert the atom to insert
     * @param posX the x offset from the molecule's pos
     * @param posY the y offset from the molecule's pos
     * @return the newly created atom
     */
    fun createLabel(molecule: EntityLevel, text: String, posX: Float, posY: Float, scale: Float = MolGLide.GLOBAL_SCALE) : EntityLevel {
        //1) Create a new entity for the atom/label to sit in from the molecule entity
        val label = molecule.addEntity()
        label.addComponent(TransformComponent(posX, posY, NewOrganicEditorState.XY_PLANE))
        label.addComponent(TextComponent(text))
        return label
    }

    fun createBond(molecule: EntityLevel, atomA: EntityLevel, atomB: EntityLevel) : EntityLevel {
        val bond = molecule.addEntity()
        bond.addComponent(TransformComponent(0.0f, 0.0f, 0.0f))
        bond.addComponent(LineDrawerComponent(atomA.id, atomB.id))
        return bond
    }

    fun tagAsAtom(atom: EntityLevel) {
        atom.addComponent(AtomComponent())
    }

    fun tagAsExplicit(atom: EntityLevel) {
        atom.addComponent(AlwaysExplicit())
    }

    fun removeAsExplicit(atom: EntityLevel) {
        if (atom.hasComponent(AlwaysExplicit::class)) {
            atom.removeComponent(AlwaysExplicit::class)
        }
    }

    fun getLvlMolFromLvlAtom(atom: EntityLevel) : UUID? {
        if (!atom.hasComponent(AtomComponent::class)) {
            return null
        }
        if (atom.hasComponent(LevelParentComponent::class)) {
            val parentID = atom.getComponent(LevelParentComponent::class).levelParentID

            return parentID
        }

        return null
    }

    fun linkParentLevel(atom: EntityLevel, parent: EntityLevel) {

        if (!atom.hasComponent(AtomComponent::class)) {
            return
        }

        atom.addComponent(LevelParentComponent(parent.id))
    }
}