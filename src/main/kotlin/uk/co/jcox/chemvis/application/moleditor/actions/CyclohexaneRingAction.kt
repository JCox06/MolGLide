package uk.co.jcox.chemvis.application.moleditor.actions

import org.apache.jena.sparql.pfunction.library.str
import org.joml.Math
import uk.co.jcox.chemvis.application.chemengine.IMoleculeManager
import uk.co.jcox.chemvis.application.moleditor.LevelViewUtil
import uk.co.jcox.chemvis.application.moleditor.OrganicEditorState
import uk.co.jcox.chemvis.cvengine.scenegraph.EntityLevel
import uk.co.jcox.chemvis.cvengine.scenegraph.TransformComponent
import java.util.UUID


class CyclohexaneRingAction (
    clickX: Float,
    clickY: Float,
    val makeBenzene: Boolean
) : TemplateAction(clickX, clickY) {

    /**
     * Adds 6 carbons in a ring hexagon with 120 degrees between each bond
     */
    override fun formCarbonNetwork(molManager: IMoleculeManager, levelMolecule: EntityLevel, structMolecule: UUID) {
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
}