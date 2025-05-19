package uk.co.jcox.chemvis.application.moleditor

import org.checkerframework.checker.units.qual.mol
import org.joml.Vector3f
import uk.co.jcox.chemvis.application.ChemVis
import uk.co.jcox.chemvis.application.chemengine.IMoleculeManager
import uk.co.jcox.chemvis.cvengine.scenegraph.EntityLevel
import uk.co.jcox.chemvis.cvengine.scenegraph.TextComponent
import uk.co.jcox.chemvis.cvengine.scenegraph.TransformComponent
import java.util.UUID

abstract class EditorAction {

    abstract fun execute(molManager: IMoleculeManager, level: EntityLevel)

    //Add an atom to an existing molecule (Only does atom, no bonds, etc.)
    protected fun createAtomLevelView(moleculeEntity: EntityLevel, molManagerAtomID: UUID, element: String, posX: Float, posY: Float) {
        //1) Create a new Entity for this atom using a local moleculeEntity as the root
        val atom = moleculeEntity.addEntity()

        atom.addComponent(MolIDComponent(molManagerAtomID))
        atom.addComponent(TransformComponent(posX, posY, 0.0f))
        atom.addComponent(TextComponent(element, ChemVis.FONT, 1.0f, 1.0f, 1.0f, ChemVis.GLOBAL_SCALE))

        //2) Add the selection marker for this atom
        //When the mouse is in a close enough range, the selection marker for this atom is shown
        
    }
}


//Fired when the user creates a new molecule containing an atom.
//(If the user clicks in the level on empty space with an insertion tool, this method will run)
class AtomCreationAction (
    private val xPos: Float,
    private val yPos: Float,
    private val element: String,

) : EditorAction() {



    override fun execute(molManager: IMoleculeManager, level: EntityLevel) {
        //1) Create a new molecule using the molManager and add the atom into it
        val newMolecule = molManager.createMolecule()
        val firstAtom = molManager.addAtom(newMolecule, element)

        //2) Update spatial representation on the Level
        val moleculeNode = level.addEntity()
        moleculeNode.addComponent(MolIDComponent(newMolecule))
        moleculeNode.addComponent(TransformComponent(xPos, yPos, 0.0f))

        createAtomLevelView(moleculeNode, firstAtom, element, 0.0f, 0.0f)
    }
}