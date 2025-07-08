package uk.co.jcox.chemvis.application.moleditor.actions

import org.joml.Math
import uk.co.jcox.chemvis.application.chemengine.IMoleculeManager
import uk.co.jcox.chemvis.application.moleditor.OrganicEditorState
import uk.co.jcox.chemvis.cvengine.scenegraph.EntityLevel
import java.util.UUID

class CyclopentaneRingAction(clickX: Float, clickY: Float) : TemplateAction(clickX, clickY){

    override fun formCarbonNetwork(molManager: IMoleculeManager, levelMolecule: EntityLevel, structMolecule: UUID) {

        val distanceFromCentreToVertex = ( OrganicEditorState.CONNECTION_DIST / 2) / Math.sin(Math.toRadians(36.0f))

        //ATOMS:
        //The carbon atom directly above
        val c1 = addCarbon(molManager, levelMolecule, structMolecule, 0.0f, distanceFromCentreToVertex)

        val x1 = distanceFromCentreToVertex * Math.cos(Math.toRadians(18.0f))
        val y1 = distanceFromCentreToVertex * Math.sin(Math.toRadians(18.0f))
        val c2 = addCarbon(molManager, levelMolecule, structMolecule, x1, y1)
        val c3 = addCarbon(molManager, levelMolecule, structMolecule, -x1, y1)


        val x2 = distanceFromCentreToVertex * Math.cos(Math.toRadians(54.0f))
        val y2 = distanceFromCentreToVertex * Math.sin(Math.toRadians(54.0f))
        val c4 = addCarbon(molManager, levelMolecule, structMolecule, x2, -y2)
        val c5 = addCarbon(molManager, levelMolecule, structMolecule, -x2, -y2)

        //BONDS:
        formCarbonBond(molManager, levelMolecule, c1, c2)
        formCarbonBond(molManager, levelMolecule, c2, c4)
        formCarbonBond(molManager, levelMolecule, c5, c4)
        formCarbonBond(molManager, levelMolecule, c5, c3)
        formCarbonBond(molManager, levelMolecule, c3, c1)
    }
}