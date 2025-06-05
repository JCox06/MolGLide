package uk.co.jcox.chemvis.application.moleditor

import org.joml.Vector3f
import org.joml.minus
import uk.co.jcox.chemvis.application.ChemVis
import uk.co.jcox.chemvis.application.chemengine.IMoleculeManager
import uk.co.jcox.chemvis.cvengine.LevelRenderer
import uk.co.jcox.chemvis.cvengine.scenegraph.EntityLevel
import uk.co.jcox.chemvis.cvengine.scenegraph.LineDrawerComponent
import uk.co.jcox.chemvis.cvengine.scenegraph.ObjComponent
import uk.co.jcox.chemvis.cvengine.scenegraph.TextComponent
import uk.co.jcox.chemvis.cvengine.scenegraph.TransformComponent
import java.util.UUID


//todo This class is literately the worst class of the entire project
//Needs complete re-write!

abstract class EditorAction {

    protected abstract fun execute(molManager: IMoleculeManager, level: EntityLevel) : UUID?

    fun runAction(molManager: IMoleculeManager, level: EntityLevel) {
        val moleculeChanged = execute(molManager, level)
        if (moleculeChanged != null) {
            molManager.recalculate(moleculeChanged)
        }
    }

    //Add an atom to an existing molecule (Only does atom, no bonds, etc.)
    protected fun createAtomLevelView(moleculeEntity: EntityLevel, molManagerAtomID: UUID?, element: String, posX: Float, posY: Float, quantity: Int = 1) : EntityLevel {
        //1) Create a new Entity for this atom using a local moleculeEntity as the root
        val atom = moleculeEntity.addEntity()


        atom.addComponent(TransformComponent(posX, posY, OrganicEditorState.XY_PLANE, 1.0f))

        if (molManagerAtomID != null) {
            atom.addComponent(TextComponent(element, ChemVis.FONT, 1.0f, 1.0f, 1.0f, ChemVis.GLOBAL_SCALE))
        } else {
            atom.addComponent(TextComponent("${element}${quantity}", ChemVis.FONT, 1.0f, 1.0f, 1.0f, ChemVis.GLOBAL_SCALE))
        }

        //2) Add the selection marker for this atom
        //When the mouse is in a close enough range, the selection marker for this atom is shown


        if (molManagerAtomID != null) {
            atom.addComponent(MolIDComponent(molManagerAtomID))
            val selectionMarkerEntity = atom.addEntity()
            selectionMarkerEntity.addComponent(TransformComponent(0.0f, 0.0f, -10.0f, OrganicEditorState.SELECTION_RADIUS))
            selectionMarkerEntity.addComponent(ObjComponent(ChemVis.SELECTION_MARKER_MESH))
            selectionMarkerEntity.getComponent(TransformComponent::class).visible = false

            atom.addComponent(MolSelectionComponent(selectionMarkerEntity.id))


            //create inline anchors
            createInlineDistAnchors(atom)
        } else {
            atom.addComponent(GhostImplicitHydrogenGroupComponent())
        }

        return atom
    }


    //Add a bond to an existing molecule with existing atoms (AtomA, AtomB)
    protected fun createSingleBondLevelView(moleculeEntity: EntityLevel, atomA: EntityLevel, atomB: EntityLevel, bondMolManID: UUID) {
        //1) Create a new bond for this atom using a local moleculeEntity as root
        val l_bond = moleculeEntity.addEntity()
        l_bond.addComponent(MolIDComponent(bondMolManID))
        //Atom B is the old one
        l_bond.addComponent(LineDrawerComponent(atomA.id, atomB.id, 2.0f))
        l_bond.addComponent(TransformComponent(0.0f, 0.0f, 0.0f))
    }

    protected fun createInlineDistAnchors(lAtom: EntityLevel) {
        //These are little anchors at 90 degrees to atoms. By default they are invisible. When you enter "Inline Bond Mode" they reveal themselves
        //When clicked these anchors open a menu to allow you to add inline atoms without a physical bond Entity being created
        //For example usually the -OH group on alcohols are inline, because no bond is shown on the OH alcohol

        //First Anchor
        var tempAnchor = lAtom.addEntity()
        tempAnchor.addComponent(TransformComponent(OrganicEditorState.INLINE_DIST, 0.0f, 0.0f, 1.0f, false))
        tempAnchor.addComponent(ObjComponent(ChemVis.INLINE_ANCHOR_MESH))
        tempAnchor.addComponent(AnchorComponent())

        tempAnchor = lAtom.addEntity()
        tempAnchor.addComponent(TransformComponent(-OrganicEditorState.INLINE_DIST, 0.0f, 0.0f, 1.0f, false))
        tempAnchor.addComponent(ObjComponent(ChemVis.INLINE_ANCHOR_MESH))
        tempAnchor.addComponent(AnchorComponent())


        tempAnchor = lAtom.addEntity()
        tempAnchor.addComponent(TransformComponent(0.0f, OrganicEditorState.INLINE_DIST, 0.0f, 1.0f, false))
        tempAnchor.addComponent(ObjComponent(ChemVis.INLINE_ANCHOR_MESH))
        tempAnchor.addComponent(AnchorComponent())

        tempAnchor = lAtom.addEntity()
        tempAnchor.addComponent(TransformComponent(0.0f, -OrganicEditorState.INLINE_DIST, 0.0f, 1.0f, false))
        tempAnchor.addComponent(ObjComponent(ChemVis.INLINE_ANCHOR_MESH))
        tempAnchor.addComponent(AnchorComponent())
        //Carry on for the rest...

    }


    protected fun checkImplictCarbons(molManager: IMoleculeManager, levelAtom: EntityLevel, molMolecule: UUID, molAtom: UUID) {
        if (molManager.isOfElement(molMolecule, molAtom, "C")) {
            if (molManager.getBonds(molMolecule, molAtom) >= OrganicEditorState.CARBON_IMPLICIT_LIMIT) {
                val transform = levelAtom.getComponent(TransformComponent::class)
                transform.visible = false


                val toRemove: MutableList<EntityLevel> = mutableListOf()

                levelAtom.traverseFunc {
                    if (it.hasComponent(GhostImplicitHydrogenGroupComponent::class)) {
                        toRemove.add(it)
                    }
                }

                toRemove.forEach { levelAtom.removeEntity(it) }
            }
        }
    }

    protected fun removeImplicitHydrogensFromStructure(molManager: IMoleculeManager, molecule: UUID, atom: UUID) {
        molManager.removeImplicitHydrogenIfPossible(molecule, atom)
    }
}


//Fired when the user creates a new molecule containing an atom.
//(If the user clicks in the level on empty space with an insertion tool, this method will run)
class AtomCreationAction (
    private val xPos: Float,
    private val yPos: Float,
    private val element: String,
    private val addImplicitHydrogens: Boolean

) : EditorAction() {



     override fun execute(molManager: IMoleculeManager, level: EntityLevel) : UUID? {
        //1) Create a new molecule using the molManager and add the atom into it
        val newMolecule = molManager.createMolecule()
        val firstAtom = molManager.addAtom(newMolecule, element)

        //2) Update spatial representation on the Level
        val moleculeNode = level.addEntity()
        moleculeNode.addComponent(MolIDComponent(newMolecule))
        moleculeNode.addComponent(TransformComponent(xPos, yPos, OrganicEditorState.XY_PLANE, 1.0f))

        val firstAtomLevel = createAtomLevelView(moleculeNode, firstAtom, element, 0.0f, 0.0f)

        if (addImplicitHydrogens && element == "C") {
            val added = molManager.addImplicitHydrogens(newMolecule, firstAtom)
            createAtomLevelView(firstAtomLevel, null, "H", OrganicEditorState.INLINE_DIST, 0.0f, added)
        }

         return newMolecule
    }
}


//Fired when the user wants to add an atom to an existing molecule
class AtomInsertionAction (
    private val xPos: Float,
    private val yPos: Float,
    private val element: String,
    private val moleculeEntity: EntityLevel,
    private val preExistingSelection: EntityLevel,
    private val checkImplicitCarbons: Boolean,
    private val checkImplicitHydrogens: Boolean,
) : EditorAction() {

    //todo needs to be position of the bond of the last one
    lateinit var insertedAtom: UUID

    override fun execute(molManager: IMoleculeManager, level: EntityLevel) : UUID? {


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


        removeImplicitHydrogensFromStructure(molManager, molecule.molID, m_preExisting.molID)

        if (checkImplicitHydrogens) {
            val a = molManager.addImplicitHydrogens(molecule.molID, newAtomID)

        }

        if (checkImplicitCarbons) {
            checkImplictCarbons(molManager, preExistingSelection, molecule.molID, m_preExisting.molID)
            checkImplictCarbons(molManager, insertedAtomEntity, molecule.molID, newAtomID)
        }

        return molecule.molID;
    }
}


class AtomInsertionInlineAction(
    private val anchor: EntityLevel,
    private val element: String,

    //todo - Implement the Quantity
    //and at some point - make it so different elements can be present in the Inline
    private val quantity: Int

) : EditorAction() {
    override fun execute(molManager: IMoleculeManager, level: EntityLevel) : UUID? {

        val positionForPlacement = anchor.getAbsolutePosition()

        val anchorTransform = anchor.getComponent(TransformComponent::class)
        anchorTransform.visible = false

        val atomOfAnchor = anchor.parent
        val parentMolecule = atomOfAnchor?.parent
        val cdkMolId = parentMolecule?.getComponent(MolIDComponent::class)
        val cdkAtomOfAnchor = atomOfAnchor?.getComponent(MolIDComponent::class)

        if (parentMolecule != null && cdkMolId != null) {

            val molTransform = parentMolecule.getAbsolutePosition()
            val localPos = positionForPlacement - molTransform

            val newAtom = molManager.addAtom(cdkMolId.molID, element)
            createAtomLevelView(parentMolecule, newAtom, element, localPos.x, localPos.y)

            //Form a single bond between old atom and new atom
            molManager.formBond(cdkMolId.molID, atomOfAnchor.getComponent(MolIDComponent::class).molID, newAtom, 1)

            return cdkMolId.molID
        }
        return null
    }


}