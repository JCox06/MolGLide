package uk.co.jcox.chemvis.application.moleditorstate.action

import uk.co.jcox.chemvis.application.graph.ChemAtom
import uk.co.jcox.chemvis.application.graph.LevelContainer
import uk.co.jcox.chemvis.application.moleditorstate.AtomInsert

class AtomReplacementAction (
    private val atom: ChemAtom,
    private val toReplace: AtomInsert,
) : IAction {

    private var oldAtom = AtomInsert.CARBON

    override fun execute(levelContainer: LevelContainer) {

        oldAtom = levelContainer.chemManager.getAtomInsert(atom.molManagerLink)

        levelContainer.chemManager.replace(atom.molManagerLink, toReplace)

        levelContainer.chemManager.recalculate(atom.parent.molManagerLink)

        atom.visible = toReplace != AtomInsert.CARBON

    }

    override fun undo(levelContainer: LevelContainer) {
        levelContainer.chemManager.replace(atom.molManagerLink, oldAtom)

        levelContainer.chemManager.recalculate(atom.parent.molManagerLink)


        atom.visible = oldAtom != AtomInsert.CARBON
    }


}