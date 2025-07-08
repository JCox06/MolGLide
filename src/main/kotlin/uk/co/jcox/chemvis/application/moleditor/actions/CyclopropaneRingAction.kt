package uk.co.jcox.chemvis.application.moleditor.actions

import org.openscience.cdk.smiles.smarts.parser.SMARTSParserConstants.c
import uk.co.jcox.chemvis.application.chemengine.IMoleculeManager
import uk.co.jcox.chemvis.application.moleditor.OrganicEditorState
import uk.co.jcox.chemvis.cvengine.scenegraph.EntityLevel
import java.lang.Math.pow
import java.util.UUID
import kotlin.math.pow
import kotlin.math.sqrt

class CyclopropaneRingAction(clickX: Float, clickY: Float) : TemplateAction(clickX, clickY) {


    override fun formCarbonNetwork(molManager: IMoleculeManager, levelMolecule: EntityLevel, structMolecule: UUID) {

        val commonHorizontalDistance = OrganicEditorState.CONNECTION_DIST / 2
        val commonVerticalDistance = sqrt(OrganicEditorState.CONNECTION_DIST.pow(2) - (OrganicEditorState.CONNECTION_DIST / 2).pow(2)) / 2

        val c1 = addCarbon(molManager, levelMolecule, structMolecule, 0.0f, commonVerticalDistance)
        val c2 = addCarbon(molManager, levelMolecule, structMolecule, commonHorizontalDistance, -commonVerticalDistance)
        val c3 = addCarbon(molManager, levelMolecule, structMolecule, -commonHorizontalDistance, -commonVerticalDistance)

        formCarbonBond(molManager, levelMolecule, c1, c2)
        formCarbonBond(molManager, levelMolecule, c2, c3)
        formCarbonBond(molManager, levelMolecule, c1, c3)
    }
}