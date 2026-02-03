package uk.co.jcox.chemvis.application.moleditorstate.action

import org.openscience.cdk.CDKConstants
import uk.co.jcox.chemvis.application.graph.ChemAtom
import uk.co.jcox.chemvis.application.graph.LevelContainer

class InsertExplicitMethylAction(private val atom: ChemAtom) : AtomReplacementAction(atom, "C") {

    override fun execute(levelContainer: LevelContainer) {
        super.execute(levelContainer)
        atom.priorityName = "Me"
        atom.visible = true
    }

    override fun undo(levelContainer: LevelContainer) {
        super.undo(levelContainer)
        atom.priorityName = null
    }
}