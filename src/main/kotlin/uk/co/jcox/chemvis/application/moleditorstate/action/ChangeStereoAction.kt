package uk.co.jcox.chemvis.application.moleditorstate.action

import uk.co.jcox.chemvis.application.graph.ChemBond
import uk.co.jcox.chemvis.application.graph.LevelContainer
import uk.co.jcox.chemvis.application.moleditorstate.StereoChem

class ChangeStereoAction (val bond: ChemBond, val newStereo: StereoChem) : IAction {

    var oldStereo: StereoChem? = null

    override fun execute(levelContainer: LevelContainer) {
        oldStereo = levelContainer.chemManager.getStereoChem(bond.molManagerLink)

        levelContainer.chemManager.updateStereoChem(bond.molManagerLink, newStereo)
    }

    override fun undo(levelContainer: LevelContainer) {
        oldStereo?.let {
            levelContainer.chemManager.updateStereoChem(bond.molManagerLink, it)
        }
    }
}