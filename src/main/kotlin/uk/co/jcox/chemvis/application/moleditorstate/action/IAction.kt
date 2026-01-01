package uk.co.jcox.chemvis.application.moleditorstate.action

import uk.co.jcox.chemvis.application.graph.LevelContainer

interface IAction {

    fun execute(levelContainer: LevelContainer)

    fun undo(levelContainer: LevelContainer)

    fun redo(levelContainer: LevelContainer) {
        execute(levelContainer)
    }
}