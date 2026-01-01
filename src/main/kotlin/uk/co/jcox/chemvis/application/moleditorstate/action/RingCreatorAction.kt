package uk.co.jcox.chemvis.application.moleditorstate.action

import org.openscience.cdk.Ring
import org.openscience.cdk.interfaces.IRing
import org.openscience.cdk.layout.RingPlacer
import uk.co.jcox.chemvis.application.graph.ChemAtom
import uk.co.jcox.chemvis.application.graph.ChemBond
import uk.co.jcox.chemvis.application.graph.ChemMolecule
import uk.co.jcox.chemvis.application.graph.LevelContainer
import uk.co.jcox.chemvis.application.moleditorstate.OrganicEditorState
import uk.co.jcox.chemvis.application.moleditorstate.RingInsert
import uk.co.jcox.chemvis.application.moleditorstate.SelectionManager
import javax.vecmath.Point2d

class RingCreatorAction (
    private val clickX: Float,
    private val clickY: Float,
    private val templateInsert: RingInsert,
    private val primarySelection: SelectionManager.Type
) : IAction {

    private val ringBuilder: RingPlacer = RingPlacer()

    override fun execute(levelContainer: LevelContainer) {
        val tempRing: IRing = Ring(templateInsert.size, "C")
        ringBuilder.placeRing(tempRing, Point2d(0.0, 0.0), OrganicEditorState.CONNECTION_DISTANCE.toDouble())

        val permRing = getChemMolecule(clickX, clickY)

        tempRing.atoms().forEach { atom ->
            val chemAtom = permRing.addAtom(atom)
            chemAtom.visible = false
            atom.setProperty("LINK", chemAtom)
        }

        tempRing.bonds().forEach {bond ->
            val atomA = bond.getAtom(0).getProperty<ChemAtom>("LINK")
            val atomB = bond.getAtom(1).getProperty<ChemAtom>("LINK")
            val chemBond = permRing.addBond(atomA, atomB, bond)
        }

        levelContainer.sceneMolecules.add(permRing)
    }

    private fun getChemMolecule(clickX: Float, clickY: Float) : ChemMolecule {

        //Insert Isolated Ring
        if (primarySelection is SelectionManager.Type.None) {
            val permRing = ChemMolecule()
            permRing.positionOffset.x = clickX
            permRing.positionOffset.y = clickY
            return permRing
        }

        //Insert a Ring through a common bond
        if (primarySelection is SelectionManager.Type.ActiveBond) {

        }

        throw Exception("sdfjksdhf")
    }

    override fun undo(levelContainer: LevelContainer) {
    }
}