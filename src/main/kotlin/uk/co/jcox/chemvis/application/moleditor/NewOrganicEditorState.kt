package uk.co.jcox.chemvis.application.moleditor


import org.joml.Vector2f
import org.joml.minus
import uk.co.jcox.chemvis.application.MolGLide
import uk.co.jcox.chemvis.application.moleditor.actions.AtomCreationAction
import uk.co.jcox.chemvis.cvengine.Camera2D
import uk.co.jcox.chemvis.cvengine.IApplicationState
import uk.co.jcox.chemvis.cvengine.ICVServices
import uk.co.jcox.chemvis.cvengine.IInputSubscriber
import uk.co.jcox.chemvis.cvengine.InputManager
import uk.co.jcox.chemvis.cvengine.LevelRenderer
import uk.co.jcox.chemvis.cvengine.RawInput
import uk.co.jcox.chemvis.cvengine.scenegraph.EntityLevel
import uk.co.jcox.chemvis.cvengine.scenegraph.LineDrawerComponent
import uk.co.jcox.chemvis.cvengine.scenegraph.ObjComponent
import uk.co.jcox.chemvis.cvengine.scenegraph.TransformComponent
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

class NewOrganicEditorState (
    private val services: ICVServices,
    private val camera2D: Camera2D,
    private val levelRenderer: LevelRenderer,
) : IApplicationState, IInputSubscriber {

    private val workState = WorkState()
    private val ui = ApplicationUI()
    private val selection = SelectionManager()

    private var renderTransientBonds = false

    override fun init() {
        workState.init()
    }

    override fun update(inputManager: InputManager, timeElapsed: Float) {
        if (! inputManager.mouseClick(RawInput.MOUSE_1)) {
            val mousePos = camera2D.screenToWorld(inputManager.mousePos())
            selection.update(workState.getLevel(), mousePos.x, mousePos.y)
        }

        renderTransientBonds = selection.getPrimary() is Selection.Active && inputManager.mouseClick(RawInput.MOUSE_1)
    }

    override fun render(viewport: Vector2f) {

        val transientUI = EntityLevel()
        renderTransientSelectionMarker(transientUI)
        renderTransientBond(transientUI)
        levelRenderer.renderLevel(transientUI, camera2D, viewport)

        levelRenderer.renderLevel(workState.getLevel(), camera2D, viewport)

        ui.mainMenu(services)
        ui.renderWidgets()
    }


    override fun clickEvent(inputManager: InputManager, key: RawInput) {
        if (inputManager.keyClick(RawInput.LCTRL)) {
            if (key == RawInput.KEY_Z) {
                workState.undo()
            }
            if (key == RawInput.KEY_Y) {
                workState.redo()
            }
        }

    }

    override fun clickReleaseEvent(inputManager: InputManager, key: RawInput) {
        if (key == RawInput.MOUSE_1 && selection.getPrimary() is Selection.None) {

            workState.makeCheckpoint()

            val mousePos = camera2D.screenToWorld(inputManager.mousePos())

            val action = AtomCreationAction(mousePos.x, mousePos.y, ui.getActiveElement())
            action.runAction(workState.getStruct(), workState.getLevel())
        }
    }

    private fun renderTransientSelectionMarker(transientUI: EntityLevel) {
       val primary = selection.getPrimary()
        if (primary is Selection.Active) {
            val entity = workState.getLevel().findByID(primary.id)
            val position = entity?.getAbsolutePosition()

            if (position == null) {
                return
            }

            val ui = transientUI.addEntity()
            ui.addComponent(TransformComponent(position.x, position.y, position.z, SELECTION_MARKER_SIZE))
            ui.addComponent(ObjComponent(MolGLide.SELECTION_MARKER_MESH, MolGLide.SELECTION_MARKER_MATERIAL))
        }
    }

    private fun renderTransientBond(transientUI: EntityLevel) {

        if (!renderTransientBonds) {
            return
        }

        val mousePos = camera2D.screenToWorld(services.inputs().mousePos())

        val primary = selection.getPrimary()
        if (primary is Selection.Active) {
            val entity = workState.getLevel().findByID(primary.id)
            val position = entity?.getAbsolutePosition()

            if (position == null) {
                return
            }

            val closestPoint = closestPointToCircleCircumference(Vector2f(position.x, position.y), mousePos, CONNECTION_DIST)
            val connectionHolder = transientUI.addEntity()
            connectionHolder.addComponent(TransformComponent(closestPoint.x, closestPoint.y, XY_PLANE))
            val line = transientUI.addEntity()
            val entityInTransient = transientUI.addEntity()
            entityInTransient.addComponent(TransformComponent(position.x, position.y, position.z))
            line.addComponent(TransformComponent(0.0f, 0.0f, 0.0f))
            line.addComponent(LineDrawerComponent(entityInTransient.id, connectionHolder.id, BOND_WIDTH))
        }

    }

    override fun cleanup() {

    }

    private fun closestPointToCircleCircumference(circleCentre: Vector2f, randomPoint: Vector2f, radius: Float, quantize: Int = 16) : Vector2f {
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

    companion object {
        const val XY_PLANE = -1.0f
        const val SELECTION_RADIUS = 15.0f
        const val SELECTION_MARKER_SIZE = 10.0f
        const val INLINE_DIST = 10.0f
        const val CONNECTION_DIST = 35.0f
        const val BOND_WIDTH = 2.5f
    }
}