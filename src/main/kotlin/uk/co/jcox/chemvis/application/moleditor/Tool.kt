package uk.co.jcox.chemvis.application.moleditor

import org.joml.Vector2f
import org.joml.minus
import uk.co.jcox.chemvis.cvengine.scenegraph.EntityLevel
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin


abstract class Tool (
    val context: ToolCreationContext,
) {

    var actionInProgress = false
    protected set

    //Upon class init, get a copy of the current state
    //So we can safely mutate it without worrying about the actual real state
    private var workStateCopy = context.levelStack.get()

    private var commit: ((ChemLevelPair) -> Unit)? = null

    abstract fun updateProposedModifications()

    abstract fun renderTransientUI(transientUI: EntityLevel)

    abstract fun processClick(clickDetails: ClickContext)

    abstract fun processClickRelease(clickDetails: ClickContext)

    abstract fun update()


    fun setCommit(func: (ChemLevelPair) -> Unit) {
        commit = func
    }


    protected fun pushChanges() {
        //Ask the app state to push the latest changes to the stack
        //Then refresh our workStateCopy

        commit?.invoke(workStateCopy)

        workStateCopy = context.levelStack.get()
        actionInProgress = false


    }


    fun refreshWorkingState() {
        workStateCopy = context.levelStack.get()
        actionInProgress = false
    }

    fun getWorkingState() : ChemLevelPair {
        return workStateCopy
    }

    protected fun closestPointToCircleCircumference(circleCentre: Vector2f, randomPoint: Vector2f, radius: Float, quantize: Int = 16) : Vector2f {
//        val magCentreRandomPoint = (randomPoint - circleCentre).length()
//        val centreRandomPoint = (randomPoint - circleCentre)
//        val position = circleCentre + (centreRandomPoint.div(magCentreRandomPoint)).mul(radius)
//        return position

        val angleStep = (Math.PI * 2) / quantize

        val direction = randomPoint - circleCentre //Direction to point in space

        //Find the angle of this vector (between the positive x axis and the point tan(x) = o/a)
        val angle = atan2(direction.y, direction.x)
        val quantizedAngleIndex = (angle / angleStep).roundToInt()
        val quantizedAngle = quantizedAngleIndex * angleStep

        //Turn polar angle into cartesian coordinates
        val x = circleCentre.x + radius * cos(quantizedAngle)
        val y = circleCentre.y + radius * sin(quantizedAngle)
        return Vector2f(x.toFloat(), y.toFloat())
    }
}