package uk.co.jcox.chemvis.application.moleditorstate.action

import org.openscience.cdk.AtomContainer
import org.openscience.cdk.Ring
import org.openscience.cdk.interfaces.IAtom
import org.openscience.cdk.interfaces.IBond
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
import javax.vecmath.Vector2d

class RingCreatorAction (
    private val clickX: Float,
    private val clickY: Float,
    private val templateInsert: RingInsert,
    private val primarySelection: SelectionManager.Type
) : IAction {

    private val ringBuilder: RingPlacer = RingPlacer()

    private var placedRing: ChemMolecule? = null

    override fun execute(levelContainer: LevelContainer) {
        val tempRing: IRing = Ring(templateInsert.size, "C")

        ringBuilder.placeRing(tempRing, Point2d(0.0, 1.0), OrganicEditorState.CONNECTION_DISTANCE.toDouble())

        val permRing = ChemMolecule()
        permRing.positionOffset.x = clickX
        permRing.positionOffset.y = clickY

        copyNewData(permRing, tempRing.atoms(), tempRing.bonds())

        levelContainer.sceneMolecules.add(permRing)

        placedRing = permRing
    }




    private fun copyNewData(permRing: ChemMolecule, iAtoms: Iterable<IAtom>, iBonds: Iterable<IBond>) {
        iAtoms.forEach { iAtom ->
            val chemAtom = permRing.addAtom(iAtom)
            chemAtom.visible = false
            iAtom.setProperty("LINK", chemAtom)
        }

        var double = false

        iBonds.forEach { iBond ->
            val atomA = iBond.getAtom(0).getProperty<ChemAtom>("LINK")
            val atomB = iBond.getAtom(1).getProperty<ChemAtom>("LINK")

            val chemBond = permRing.addBond(atomA, atomB, iBond)
            if (templateInsert == RingInsert.BENZENE) {
                chemBond.flipDoubleBond = true
                if (double) {
                    permRing.updateBondOrder(chemBond, IBond.Order.DOUBLE)
                }
                permRing.setAromaticity(chemBond, true)
            }
            double = !double
        }
    }


    override fun undo(levelContainer: LevelContainer) {
        placedRing?.let { levelContainer.sceneMolecules.remove(it) }
    }

    override fun redo(levelContainer: LevelContainer) {
        placedRing?.let { levelContainer.sceneMolecules.add(it) }
    }
}