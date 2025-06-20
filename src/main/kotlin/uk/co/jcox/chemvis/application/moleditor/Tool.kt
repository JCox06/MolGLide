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

    var workingState: ChemLevelPair = context.levelStack.get()
        private set

    private var localStack: WorkState = WorkState()

    private var commit: ((ChemLevelPair) -> Unit)? = null

    init {
        refreshWorkingState()
    }

    fun refreshWorkingState(refreshLocalStack: Boolean = true) {
        this.workingState = context.levelStack.get()

        if (refreshLocalStack) {
            this.localStack = WorkState()
        }
        actionInProgress = false
    }

    protected fun makeRestorePoint() {
        localStack.makeCheckpoint(workingState)
    }

    protected fun restoreOnce() {
        if (localStack.size() < 1) {
            return
        }
        this.workingState = localStack.get()
        localStack.undo()
    }

    fun onCommit(func: (ChemLevelPair) -> Unit) {
        this.commit = func
    }

    protected fun pushChanges() {
        commit?.invoke(workingState)

        refreshWorkingState()
    }

    abstract fun renderTransientUI(transientUI: EntityLevel)

    abstract fun processClick(clickDetails: ClickContext)

    abstract fun processClickRelease(clickDetails: ClickContext)

    abstract fun update()


    protected fun closestPointToCircleCircumference(circleCentre: Vector2f, randomPoint: Vector2f, radius: Float, quantize: Int = 16) : Vector2f {
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