package uk.co.jcox.chemvis.application.moleditorstate.action

import org.openscience.cdk.interfaces.IBond
import uk.co.jcox.chemvis.application.graph.ChemAtom
import uk.co.jcox.chemvis.application.graph.ChemBond
import uk.co.jcox.chemvis.application.graph.LevelContainer
import uk.co.jcox.chemvis.application.moleditorstate.AtomInsert

/**
 * Although this class will insert an atom into the world
 * It does not require a position.
 *
 * The position is handled by AtomBondTool when it is in Dragging mode, this is finalised when a key is released
 */
class AtomInsertionAction (
    private val atomInsert: AtomInsert,
    private val stereoChem: IBond.Display,
    private val srcAtom: ChemAtom,
    private var clickX: Float,
    private var clickY: Float,
) : IAction {

    private val srcMol = srcAtom.parent

    var newLevelAtom: ChemAtom? = null
    var newLevelBond: ChemBond? = null

    private var wasVisibleBefore = false

    override fun execute(levelContainer: LevelContainer) {
       //Turn off required hydrogens
        disableHydrogensForCarbon(levelContainer)

        val pos = srcMol.positionOffset

        val newAtom = addAtom(atomInsert, clickX - pos.x, clickY - pos.y,)
        val newBond = addBond(srcAtom, newAtom)

        if (atomInsert == AtomInsert.CARBON) {
            newAtom.visible = false
        }

        newLevelAtom = newAtom
        newLevelBond = newBond
    }


    private fun addAtom(atomInsert: AtomInsert, innerX: Float, innerY: Float) : ChemAtom {
        val old = newLevelAtom
        if (old != null) {
            return srcMol.addAtom(old.iAtom)
        }
        return srcMol.addAtom(atomInsert, innerX, innerY)
    }

    private fun addBond(srcAtom: ChemAtom, newAtom: ChemAtom) : ChemBond {
        val old = newLevelBond
        if (old != null) {
            return srcMol.addBond(srcAtom, newAtom, old.iBond)
        }
        return srcMol.formBasicConnection(srcAtom, newAtom)
    }

    override fun undo(levelContainer: LevelContainer) {
        srcAtom.visible = wasVisibleBefore

        val atomToRemove = newLevelAtom
        val bondToRemove = newLevelBond

        if (atomToRemove != null && bondToRemove != null) {
            srcMol.removeAtom(atomToRemove)
            srcMol.removeBond(bondToRemove)

            val pos = atomToRemove.getWorldPosition()
            clickX = pos.x
            clickY = pos.y
        }

    }


    private fun disableHydrogensForCarbon(levelContainer: LevelContainer) {
        wasVisibleBefore = srcAtom.visible


        val srcAtomSymbol = srcAtom.getSymbol()

        if (srcAtomSymbol == AtomInsert.CARBON.symbol) {
            srcAtom.visible = false
        }
    }
}