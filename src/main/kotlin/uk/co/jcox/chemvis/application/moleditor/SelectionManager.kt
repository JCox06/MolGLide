package uk.co.jcox.chemvis.application.moleditor

import org.joml.Vector3f
import org.joml.minus
import uk.co.jcox.chemvis.cvengine.scenegraph.EntityLevel

class SelectionManager {

    var primarySelection: Selection = Selection.None


    fun update(level: EntityLevel, xPos: Float, yPos: Float) {
        primarySelection = findPrimarySelection(level, Vector3f(xPos, yPos, OrganicEditorState.XY_PLANE))
    }


    private fun findPrimarySelection(level: EntityLevel, vecPos: Vector3f) : Selection {
        var priSel: Selection = Selection.None
        level.traverseFunc {
            val absPos = it.getAbsolutePosition()

            val diffVec = absPos - vecPos

            if (diffVec.length() <= MINIMUM_SELECTION_DISTANCE && it.hasComponent(AtomComponent::class)) {
                priSel = Selection.Active(it.id)
                return@traverseFunc
            }
        }
        return priSel
    }

    fun getPrimary() : Selection {
        return primarySelection
    }

    companion object {
        const val MINIMUM_SELECTION_DISTANCE = 30.0f
    }
}