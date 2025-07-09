package uk.co.jcox.chemvis.application.moleditor.tools

import org.joml.GeometryUtils.normal
import org.joml.Math
import org.joml.Vector2f
import org.joml.minus
import org.openscience.cdk.smiles.smarts.parser.SMARTSParserConstants.x
import org.xmlcml.euclid.Angle
import uk.co.jcox.chemvis.application.MolGLide
import uk.co.jcox.chemvis.application.moleditor.ChemLevelPair
import uk.co.jcox.chemvis.application.moleditor.ClickContext
import uk.co.jcox.chemvis.application.moleditor.OrganicEditorState
import uk.co.jcox.chemvis.application.moleditor.Selection
import uk.co.jcox.chemvis.application.moleditor.ToolCreationContext
import uk.co.jcox.chemvis.application.moleditor.WorkState
import uk.co.jcox.chemvis.cvengine.scenegraph.EntityLevel
import uk.co.jcox.chemvis.cvengine.scenegraph.ObjComponent
import uk.co.jcox.chemvis.cvengine.scenegraph.TransformComponent
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * A tool is an object that can "work" on the workstate.
 * A tool changes the workstate through actions which it runs.
 *
 *
 * Each tool upon initialisation or refresh will fetch the latest copy of the ChemLevelPair form the main workstate
 * and store it in the workingState variable
 *
 * The tool modifies this internal state, creating restore points in the localStack workState object.
 *
 * After a commit via pushChanges, the localStack workstate is cleared, and the workingState object is refreshed.
 *
 * Subclass the Tool class to create custom tools.
 */
abstract class Tool (
    val context: ToolCreationContext,
) {

    /**
     * Does not represent the current working state like in OrganicEditorState, but rather just stores
     * the restore points.
     *
     * The reason for this, is because, this class needs direct ChemLevelPair access, and the workstate
     * will create a hard copy instead.
     */
    var workingState: ChemLevelPair = context.levelStack.get()
        private set

    /**
     * Represents the state that this tool is working on
     */
    private var localStack: WorkState = WorkState()

    /**
     * After a tool has finished working. It can alert someone with the new ChemLevelPair object via  this callback
     */
    private var commit: ((ChemLevelPair) -> Unit)? = null

    init {
        refreshWorkingState()
    }

    /**
     * Fetch a new version of the working state
     * @param refreshLocalStack optionally clear restore points (clear local stack too?)
     */
    fun refreshWorkingState(refreshLocalStack: Boolean = true) {
        this.workingState = context.levelStack.get()

        if (refreshLocalStack) {
            this.localStack = WorkState()
        }
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

    /**
     * Allows a tool to render something that isn't actually in the level
     * Like a selection marker
     *
     * @param transientUI a temp level object that is created each frame to show transient UI
     */
    abstract fun renderTransientUI(transientUI: EntityLevel)

    abstract fun processClick(clickDetails: ClickContext)

    abstract fun processClickRelease(clickDetails: ClickContext)

    /**
     * If the tool is in the process of modifying the current state but is not finished,
     * then this should return true.
     *
     * In the case of OrganicEditorState, if this is true (the tool is working) then
     * instead of rendering the main level, OrganicEditorState will render the level copy in the tool, for
     * live UI changes.
     */
    abstract fun inProgress() : Boolean

    /**
     * Should be called every frame
     */
    abstract fun update()


    //Mathematically quantising angles was wrong, this new implementation just takes a list of commonly used angles
    //And then snaps to the closest angle where the mouse is. This is simple than what was here before at v0.0.3
    protected fun closestPointToCircleCircumference(circleCentre: Vector2f, randomPoint: Vector2f, radius: Float) : Vector2f {
        val directionVec = randomPoint - circleCentre
        val angle = Vector2f(1.0f, 0.0f).angle(directionVec)

        val refinedAngle = OrganicEditorState.COMMON_ANGLES.minBy { abs(Math.toRadians(it) - angle) }

        val refinedAngeRad = Math.toRadians(refinedAngle)

        val x = circleCentre.x + radius * cos(refinedAngeRad)
        val y = circleCentre.y + radius * sin(refinedAngeRad)

        return Vector2f(x, y)
    }


    /**
     * Uses transient object (or any LeveLObject) and the selectionManager to put a circle around atoms the
     * mouse is close to
     */
    protected fun renderSelectionMarkerOnAtoms(transientUI: EntityLevel) {
        val currentSelection = context.selectionManager.primarySelection
        if (currentSelection !is Selection.Active) {
            return
        }

        val entitySelected = workingState.level.findByID(currentSelection.id)
        val position = entitySelected?.getAbsolutePosition()
        if (position == null) {
            return
        }

        val selectionMarker = transientUI.addEntity()
        selectionMarker.addComponent(TransformComponent(position.x, position.y, position.z, OrganicEditorState.Companion.SELECTION_MARKER_SIZE))
        selectionMarker.addComponent(ObjComponent(MolGLide.SELECTION_MARKER_MESH, MolGLide.SELECTION_MARKER_MATERIAL))
    }


    /**
     * Returns the position of the mouse in worldspace from the RenderingTargetContext
     * This should be as it takes into account the position of the RenderingTarget position
     */
    protected fun mouseWorld() : Vector2f {
        val mousePos = context.renderingContext.getMousePos(context.inputManager)

        return context.camera2D.screenToWorld(mousePos)
    }
}