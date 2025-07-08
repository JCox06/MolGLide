package uk.co.jcox.chemvis.application.moleditor.actions

import org.apache.jena.sparql.pfunction.library.str
import org.joml.Math
import uk.co.jcox.chemvis.application.chemengine.IMoleculeManager
import uk.co.jcox.chemvis.application.moleditor.LevelViewUtil
import uk.co.jcox.chemvis.application.moleditor.OrganicEditorState
import uk.co.jcox.chemvis.cvengine.scenegraph.EntityLevel
import uk.co.jcox.chemvis.cvengine.scenegraph.TransformComponent
import java.util.UUID

/**
 * This class is designed to add a cyclohexane ring to the CLP as a new molecule
 * The FuzeCyclohexane refers to adding cyclohexane to another atom in a molecule, or between two atoms in a molecule (between a bond)
 * As usual this class requires a CLP to work on
 *
 *  Note that the coordinates below represent the centre of the benzene ring.
 *  Therefore, the positions of the atoms in the ring, will be around the click pos
 *
 * These properties of the position are in world coordinates:
 * @property clickX the X position the user has clicked in the level
 * @property clickY the Y position the user has clicked in the level
 * @property makeBenzene make sure each other bond is a double bond

 *
 */
class CyclohexaneRingAction (
    val clickX: Float,
    val clickY: Float,
    val makeBenzene: Boolean
) : EditorAction() {

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

        return structMolecule
    }


    /**
     * Adds 6 carbons in a ring hexagon with 120 degrees between each bond
     */
    private fun formCarbonNetwork(molManager: IMoleculeManager, levelMolecule: EntityLevel, structMolecule: UUID) {
        //Add the carbons to the molecule:
        //As for the other carbons, that don't lie on the line from the centre,
        val commonHorizontalDistance = Math.cos(Math.toRadians(30.0f)) * OrganicEditorState.CONNECTION_DIST
        val commonVerticalDistance = OrganicEditorState.CONNECTION_DIST / 2.0f


        val c1 = addCarbon(molManager, levelMolecule, structMolecule, 0.0f, OrganicEditorState.CONNECTION_DIST)
        val c2 = addCarbon(molManager, levelMolecule, structMolecule, -commonHorizontalDistance, commonVerticalDistance)
        val c3 = addCarbon(molManager, levelMolecule, structMolecule, -commonHorizontalDistance, -commonVerticalDistance)
        val c4 = addCarbon(molManager, levelMolecule, structMolecule, 0.0f, -OrganicEditorState.CONNECTION_DIST)
        val c5 = addCarbon(molManager, levelMolecule, structMolecule, commonHorizontalDistance, -commonVerticalDistance)
        val c6 = addCarbon(molManager, levelMolecule, structMolecule, commonHorizontalDistance, commonVerticalDistance)

        formCarbonBond(molManager, levelMolecule, c1, c2)
        formCarbonBond(molManager, levelMolecule, c2, c3)
        formCarbonBond(molManager, levelMolecule, c3, c4)
        formCarbonBond(molManager, levelMolecule, c4, c5)
        formCarbonBond(molManager, levelMolecule, c5, c6)
        formCarbonBond(molManager, levelMolecule, c6, c1)

        if (makeBenzene) {
            formCarbonBond(molManager, levelMolecule, c1, c2)
            formCarbonBond(molManager, levelMolecule, c3, c4)
            formCarbonBond(molManager, levelMolecule, c5, c6)
        }
    }

    private fun addCarbon(molManager: IMoleculeManager, levelMolecule: EntityLevel, structMolecule: UUID, xPos: Float, yPos: Float) : EntityLevel {
        val levelAtom = LevelViewUtil.createLabel(levelMolecule, "C", xPos, yPos)
        levelAtom.getComponent(TransformComponent::class).visible = false

        val structAtom = molManager.addAtom(structMolecule, "C")

        LevelViewUtil.linkObject(structAtom, levelAtom)
        LevelViewUtil.tagAsAtom(levelAtom)
        LevelViewUtil.linkParentLevel(levelAtom, levelMolecule)

        return levelAtom
    }


    private fun formCarbonBond(iMoleculeManager: IMoleculeManager, levelMolecule: EntityLevel, atomA: EntityLevel, atomB: EntityLevel) {
        val subAction = BondOrderAction(atomA, atomB)
        subAction.runAction(iMoleculeManager, levelMolecule)
    }
}