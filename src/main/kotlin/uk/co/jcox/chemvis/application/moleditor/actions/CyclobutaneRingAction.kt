package uk.co.jcox.chemvis.application.moleditor.actions

import uk.co.jcox.chemvis.application.chemengine.IMoleculeManager
import uk.co.jcox.chemvis.application.moleditor.OrganicEditorState
import uk.co.jcox.chemvis.cvengine.scenegraph.EntityLevel
import java.util.UUID

class CyclobutaneRingAction(clickX: Float, clickY: Float) : TemplateAction(clickX, clickY) {


    override fun formCarbonNetwork(molManager: IMoleculeManager, levelMolecule: EntityLevel, structMolecule: UUID) {

        val half = OrganicEditorState.CONNECTION_DIST / 2

        val c1 = addCarbon(molManager, levelMolecule, structMolecule, half, half)
        val c2 = addCarbon(molManager, levelMolecule, structMolecule, half, -half)
        val c3 = addCarbon(molManager, levelMolecule, structMolecule, -half, -half)
        val c4 = addCarbon(molManager, levelMolecule, structMolecule, -half, half)

        formCarbonBond(molManager, levelMolecule, c1, c2)
        formCarbonBond(molManager, levelMolecule, c2, c3)
        formCarbonBond(molManager, levelMolecule, c3, c4)
        formCarbonBond(molManager, levelMolecule, c4, c1)
    }
}