package uk.co.jcox.chemvis.application.moleditorstate.action

import org.openscience.cdk.interfaces.IBond
import uk.co.jcox.chemvis.application.graph.ChemBond
import uk.co.jcox.chemvis.application.graph.LevelContainer

class ChangeStereoAction (val bond: ChemBond, val newStereo: IBond.Display) : IAction {

    var oldStereo: IBond.Display? = null

    override fun execute(levelContainer: LevelContainer) {
        oldStereo = bond.iBond.display
        val parent = bond.atomA.parent
        parent.updateStereoDisplay(bond, newStereo)
    }

    override fun undo(levelContainer: LevelContainer) {
        oldStereo?.let {
            val parent = bond.atomA.parent
            parent.updateStereoDisplay(bond, it)
        }
    }
}