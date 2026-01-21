package uk.co.jcox.chemvis.application.moleditorstate

import uk.co.jcox.chemvis.application.graph.LevelContainer
import uk.co.jcox.chemvis.application.moleditorstate.action.IAction

class ActionManager (
    private val levelContainer: LevelContainer
) {

    private val pastActions = ArrayDeque<IAction>()
    private val discardedActions = ArrayDeque<IAction>()
    var isDirty = false
    private set

    fun executeAction(action: IAction) {
        action.execute(levelContainer)
        pastActions.addLast(action)
        isDirty = true
    }


    fun undoLastAction() {
        if (pastActions.isNotEmpty()) {
            val last = pastActions.removeLast()
            last.undo(levelContainer)
            discardedActions.addLast(last)
            isDirty = true
        }
    }

    fun restoreLastAction() {
        if (discardedActions.isNotEmpty()) {
            val last = discardedActions.removeLast()
            last.redo(levelContainer)
            pastActions.addLast(last)
            isDirty = true
        }
    }

    fun markNotDirty() {
        isDirty = false
    }

    fun clearHistory() {
        pastActions.clear()
        discardedActions.clear()
    }
}