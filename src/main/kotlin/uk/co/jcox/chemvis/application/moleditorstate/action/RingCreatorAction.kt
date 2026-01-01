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

    override fun execute(levelContainer: LevelContainer) {
        //Add isolated Ring
        if (primarySelection is SelectionManager.Type.None) {
            addIsolatedRing(levelContainer, templateInsert.size)
        }


        //Add ring through common bond
        if (primarySelection is SelectionManager.Type.ActiveBond) {
            addBondedRing(levelContainer, templateInsert.size, primarySelection.bond)
        }
    }


    private fun addBondedRing(levelContainer: LevelContainer, size: Int, bond: ChemBond) {
        //todo - Dont know how CDKs fusion bond work so write own implementation
    }

    private fun addIsolatedRing(levelContainer: LevelContainer, size: Int) {

        val tempRing: IRing = Ring(size, "C")

        ringBuilder.placeRing(tempRing, Point2d(0.0, 1.0), OrganicEditorState.CONNECTION_DISTANCE.toDouble())

        val permRing = ChemMolecule()
        permRing.positionOffset.x = clickX
        permRing.positionOffset.y = clickY

        copyNewData(permRing, tempRing.atoms(), tempRing.bonds())

        levelContainer.sceneMolecules.add(permRing)
    }



    private fun copyNewData(permRing: ChemMolecule, iAtoms: Iterable<IAtom>, iBonds: Iterable<IBond>) {
        iAtoms.forEach { iAtom ->
            val chemAtom = permRing.addAtom(iAtom)
            chemAtom.visible = false
            iAtom.setProperty("LINK", chemAtom)
        }

        iBonds.forEach { iBond ->
            val atomA = iBond.getAtom(0).getProperty<ChemAtom>("LINK")
            val atomB = iBond.getAtom(1).getProperty<ChemAtom>("LINK")
            val chemBond = permRing.addBond(atomA, atomB, iBond)
        }
    }


    override fun undo(levelContainer: LevelContainer) {
    }
}