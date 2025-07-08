package uk.co.jcox.chemvis.application.moleditor.actions

import uk.co.jcox.chemvis.application.chemengine.IMoleculeManager
import uk.co.jcox.chemvis.application.moleditor.LevelViewUtil
import uk.co.jcox.chemvis.application.moleditor.OrganicEditorState
import uk.co.jcox.chemvis.cvengine.scenegraph.EntityLevel
import uk.co.jcox.chemvis.cvengine.scenegraph.TransformComponent
import java.util.UUID

/**
 * This class is designed to add a  ring to the CLP as a new molecule
 * The FuzeTemplate refers to adding a ring to another molecule through a common bond
 * As usual this class requires a CLP to work on
 *
 *  Note that the coordinates below represent the centre of the benzene ring.
 *  Therefore, the positions of the atoms in the ring, will be around the click pos
 *
 * These properties of the position are in world coordinates:
 * @property clickX the X position the user has clicked in the level
 * @property clickY the Y position the user has clicked in the level
 * @property rootMolecule The molecule that has just been created. The tool will use this to allow rotations on the molecule
 *
 */
abstract class TemplateAction (
    protected val clickX: Float,
    protected val clickY: Float
) : EditorAction(){

    lateinit var rootMolecule: EntityLevel


    override fun execute(molManager: IMoleculeManager, level: EntityLevel): UUID? {

        //In a ring, to make rotation easy, the root position should be the centre of the ring.
        //This is a special case, as usually, the root node is of the first atom added.
        val levelMolecule = level.addEntity()
        levelMolecule.addComponent(TransformComponent(clickX, clickY, OrganicEditorState.XY_PLANE))

        //Create the chemical structure
        val structMolecule = molManager.createMolecule()

        //Link the structure to the level
        LevelViewUtil.linkObject(structMolecule, levelMolecule)

        formCarbonNetwork(molManager, levelMolecule, structMolecule)

        rootMolecule = levelMolecule

        return structMolecule
    }

    protected fun addCarbon(molManager: IMoleculeManager, levelMolecule: EntityLevel, structMolecule: UUID, xPos: Float, yPos: Float) : EntityLevel {
        val levelAtom = LevelViewUtil.createLabel(levelMolecule, "C", xPos, yPos)
        levelAtom.getComponent(TransformComponent::class).visible = false

        val structAtom = molManager.addAtom(structMolecule, "C")

        LevelViewUtil.linkObject(structAtom, levelAtom)
        LevelViewUtil.tagAsAtom(levelAtom)
        LevelViewUtil.linkParentLevel(levelAtom, levelMolecule)

        return levelAtom
    }

    protected fun formCarbonBond(iMoleculeManager: IMoleculeManager, levelMolecule: EntityLevel, atomA: EntityLevel, atomB: EntityLevel) {
        val subAction = BondOrderAction(atomA, atomB)
        subAction.runAction(iMoleculeManager, levelMolecule)
    }


    /**
     * Calculate the positions of each of the carbons in the ring and then add them to the molecule using the addCarbon and formCarbonBond helper functions
     * @param molManager The Molecule Manager for the level
     * @param levelMolecule The root molecule node for the level molecule
     * @param structMolecule the CDK representation of the molecule
     */
    abstract fun formCarbonNetwork(molManager: IMoleculeManager, levelMolecule: EntityLevel, structMolecule: UUID)
}