package uk.co.jcox.chemvis.application.moleditor

import uk.co.jcox.chemvis.application.chemengine.CDKManager
import uk.co.jcox.chemvis.application.chemengine.IMoleculeManager
import uk.co.jcox.chemvis.cvengine.scenegraph.EntityLevel

class WorkState {

    private val stack = ArrayDeque<ChemLevelPair>()
    private val redoStack = ArrayDeque<ChemLevelPair>()

    fun init() {
        stack.add(ChemLevelPair(EntityLevel(), CDKManager()))
    }

    fun makeCheckpoint() {
        stack.addLast(stack.last().clone())
    }

    fun getLevel() : EntityLevel {
        return stack.last().level
    }

    fun getStruct(): IMoleculeManager {
        return stack.last().molManager
    }


    fun undo() {
        if (stack.size > 1) {
            redoStack.add(stack.removeLast())
        }
    }

    fun redo() {
        if (redoStack.size > 1) {
            stack.add(redoStack.removeLast())
        }
    }

}