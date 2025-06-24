package uk.co.jcox.chemvis.application.moleditor

import uk.co.jcox.chemvis.application.chemengine.CDKManager
import uk.co.jcox.chemvis.application.chemengine.IMoleculeManager
import uk.co.jcox.chemvis.cvengine.scenegraph.EntityLevel
import uk.co.jcox.chemvis.cvengine.scenegraph.LineDrawerComponent
import uk.co.jcox.chemvis.cvengine.scenegraph.TextComponent

class WorkState {

    private val stack = ArrayDeque<ChemLevelPair>()
    private val redoStack = ArrayDeque<ChemLevelPair>()

    fun size() : Int  {
        return stack.size
    }

    fun init() {
        stack.add(ChemLevelPair(EntityLevel(), CDKManager()))
    }

    fun makeCheckpoint(clp: ChemLevelPair) {
        stack.addLast(clp.clone())
        redoStack.clear()
    }

    fun makeCheckpoint() {
        stack.addLast(stack.last().clone())
        redoStack.clear()
    }

    fun get() : ChemLevelPair {
        val clp = stack.last()
        val clone =  clp.clone()
        return clone
    }


    fun undo() {
        if (stack.size > 1) {
            redoStack.add(stack.removeLast())
        }
    }

    fun redo() {
        if (redoStack.isNotEmpty()) {
            stack.add(redoStack.removeLast())
        }
    }

    fun clear() {
        stack.clear()
        redoStack.clear()
    }


    fun setTextTheme(textComponent: TextComponent) {
        stack.last().level.addComponent(textComponent)
    }

    fun setLineTheme(lineDrawerComponent: LineDrawerComponent) {
        stack.last().level.addComponent(lineDrawerComponent)
    }
}