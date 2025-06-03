package uk.co.jcox.chemvis.application.moleditor

import org.checkerframework.checker.units.qual.mol
import org.joml.Vector3f
import org.joml.minus
import org.joml.plus
import uk.co.jcox.chemvis.application.ChemVis
import uk.co.jcox.chemvis.application.chemengine.IMoleculeManager
import uk.co.jcox.chemvis.cvengine.scenegraph.EntityLevel
import uk.co.jcox.chemvis.cvengine.scenegraph.LineDrawerComponent
import uk.co.jcox.chemvis.cvengine.scenegraph.ObjComponent
import uk.co.jcox.chemvis.cvengine.scenegraph.TextComponent
import uk.co.jcox.chemvis.cvengine.scenegraph.TransformComponent
import java.util.UUID

abstract class EditorAction {

    abstract fun execute(molManager: IMoleculeManager, level: EntityLevel)

    //Add an atom to an existing molecule (Only does atom, no bonds, etc.)
    protected fun createAtomLevelView(moleculeEntity: EntityLevel, molManagerAtomID: UUID, element: String, posX: Float, posY: Float) : EntityLevel {
        //1) Create a new Entity for this atom using a local moleculeEntity as the root
        val atom = moleculeEntity.addEntity()

        atom.addComponent(MolIDComponent(molManagerAtomID))
        atom.addComponent(TransformComponent(posX, posY, 0.0f, 1.0f))
        atom.addComponent(TextComponent(element, ChemVis.FONT, 1.0f, 1.0f, 1.0f, ChemVis.GLOBAL_SCALE))

        //2) Add the selection marker for this atom
        //When the mouse is in a close enough range, the selection marker for this atom is shown
        val selectionMarkerEntity = atom.addEntity()
        selectionMarkerEntity.addComponent(TransformComponent(0.0f, 0.0f, -10.0f, OrganicEditorState.SELECTION_RADIUS))
        selectionMarkerEntity.addComponent(ObjComponent(ChemVis.SELECTION_MARKER_MESH))
        selectionMarkerEntity.getComponent(TransformComponent::class).visible = false

        atom.addComponent(MolSelectionComponent(selectionMarkerEntity.id))

        return atom
    }


    //Add a bond to an existing molecule with existing atoms (AtomA, AtomB)
    protected fun createSingleBondLevelView(moleculeEntity: EntityLevel, atomA: EntityLevel, atomB: EntityLevel, bondMolManID: UUID) {
        //1) Create a new bond for this atom using a local moleculeEntity as root
        val l_bond = moleculeEntity.addEntity()
        l_bond.addComponent(MolIDComponent(bondMolManID))

        val transformAtomA = atomB.getComponent(TransformComponent::class)
        l_bond.addComponent(transformAtomA)

        val transformAtomB = atomA.getAbsolutePosition()

        l_bond.addComponent(LineDrawerComponent(Vector3f(transformAtomB.x, transformAtomB.y, transformAtomB.z - 1.0f), 1.5f))
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

        println(firstAtom)

        //2) Update spatial representation on the Level
        val moleculeNode = level.addEntity()
        moleculeNode.addComponent(MolIDComponent(newMolecule))
        moleculeNode.addComponent(TransformComponent(xPos, yPos, 0.0f, 1.0f))

        createAtomLevelView(moleculeNode, firstAtom, element, 0.0f, 0.0f)
    }
}


//Fired when the user wants to add an atom to an existing molecule
class AtomInsertionAction (
    private val xPos: Float,
    private val yPos: Float,
    private val element: String,
    private val moleculeEntity: EntityLevel,
    private val preExistingSelection: EntityLevel,
) : EditorAction() {

    lateinit var insertedAtom: UUID

    override fun execute(molManager: IMoleculeManager, level: EntityLevel) {


        val moleculePos = moleculeEntity.getAbsolutePosition()
        val localAtomPos = Vector3f(xPos, yPos, 0.0f) - moleculePos

        //1) Add the atom to the molecule using the molManager
        val molecule = moleculeEntity.getComponent(MolIDComponent::class)
        val newAtomID = molManager.addAtom(molecule.molID, element)

        //2) Add a bond in-between the selected atoms and this newly created atom
        val m_preExisting = preExistingSelection.getComponent(MolIDComponent::class)
        println(m_preExisting.molID)
        val m_bondID = molManager.formBond(molecule.molID, m_preExisting.molID, newAtomID, 1)

        //2) Update spatial representation
        val insertedAtomEntity =  createAtomLevelView(moleculeEntity, newAtomID, element, localAtomPos.x, localAtomPos.y)
        insertedAtom = insertedAtomEntity.id
        createSingleBondLevelView(moleculeEntity, preExistingSelection, insertedAtomEntity, m_bondID)
    }
}