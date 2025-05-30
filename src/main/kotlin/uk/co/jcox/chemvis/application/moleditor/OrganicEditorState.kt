package uk.co.jcox.chemvis.application.moleditor

import org.joml.Vector2f
import org.joml.Vector3f
import org.joml.minus
import org.joml.plus
import uk.co.jcox.chemvis.application.chemengine.CDKManager
import uk.co.jcox.chemvis.cvengine.Camera2D
import uk.co.jcox.chemvis.cvengine.IApplicationState
import uk.co.jcox.chemvis.cvengine.IInputSubscriber
import uk.co.jcox.chemvis.cvengine.InputManager
import uk.co.jcox.chemvis.cvengine.LevelRenderer
import uk.co.jcox.chemvis.cvengine.RawInput
import uk.co.jcox.chemvis.cvengine.scenegraph.EntityLevel
import uk.co.jcox.chemvis.cvengine.scenegraph.TransformComponent
import java.util.Stack
import java.util.UUID

class OrganicEditorState (
    private val levelRenderer: LevelRenderer,
    private val camera2D: Camera2D,
) : IApplicationState, IInputSubscriber {

    //Two things the OrganicEditorState requires.
    //1 - The IMoleculeManager which manages the chemical relations of the molecule
    //2 - A root EntityLevel to manage the 2D positions of the atoms of the molecule


    //At some point a file needs to exist that contains both of these data structures so workbooks can be loaded from disc
    private val workState: Stack<ChemLevelPair> = Stack()

    var selection: UUID? = null


    override fun init() {
        workState.push(ChemLevelPair(EntityLevel(), CDKManager()))
    }

    override fun update(inputManager: InputManager, timeElapsed: Float) {

    }

    override fun render() {
        levelRenderer.renderLevel(workState.peek().level, camera2D)
    }

    override fun cleanup() {

    }


    override fun clickEvent(inputManager: InputManager, key: RawInput) {
        //If the user clicks LCTRL, then we can do some actions
        if (inputManager.keyClick(RawInput.LCTRL)) {
            if (key == RawInput.KEY_F) {
                workState.add(workState.peek().clone())
            }

            if (key == RawInput.KEY_Z) {
                if (workState.size > 1) {
                    workState.pop()
                }
            }
        }

        //Currently assume carbon tool selected:

        if (key == RawInput.MOUSE_1) {

            workState.push(workState.peek().clone())

            val worldPos = camera2D.screenToWorld(inputManager.mousePos())


            if (selection != null) {


                val selectedEntity: EntityLevel? = workState.peek().level.findByID(selection!!)
                if (selectedEntity == null) {
                    return;
                }

                val transformAtom = selectedEntity!!.getAbsolutePosition()

                val circlePos = closestPointToCircleCircumference(Vector2f(transformAtom.x, transformAtom.y), worldPos, CONNECTION_DIST)

                val action = AtomInsertionAction(circlePos.x, circlePos.y, "C", selectedEntity!!.parent!!)
                prepareTransitionState(action)

            } else {
                val action = AtomCreationAction(worldPos.x, worldPos.y, "C")
                prepareTransitionState(action)
            }


        }
    }


    override fun clickReleaseEvent(inputManager: InputManager, key: RawInput) {


    }

    override fun mouseMoveEvent(inputManager: InputManager, xPos: Double, yPos: Double) {

        if (inputManager.mouseClick(RawInput.MOUSE_1)) {
            return;
        }

        //Check if the current mouse position has an element
        val mouseWorldPos = camera2D.screenToWorld(inputManager.mousePos())
        updateSelection(workState.peek().level, Vector3f(mouseWorldPos, 0.0f), SELECTION_MARKER_RADIUS)
    }


    override fun mouseScrollEvent(inputManager: InputManager, xScroll: Double, yScroll: Double) {
        super.mouseScrollEvent(inputManager, xScroll, yScroll)
    }


    private fun updateSelection(level: EntityLevel, mouseWorld: Vector3f, radius: Float) {

        val toProcess: MutableList<EntityLevel> = mutableListOf()

        level.traverseFunc { child ->
            if (child.hasComponent(MolIDComponent::class) && child.hasComponent(MolSelectionComponent::class)) {
                toProcess.add(child)
            }
        }


        for (entityLevel in toProcess) {
            if (entityLevel.hasComponent(MolSelectionComponent::class)) {
                val entityID = entityLevel.getComponent(MolSelectionComponent::class)
                val entity = workState.peek().level.findByID(entityID.selectionEntity)
                if (entity != null) {
                    entity.getComponent(TransformComponent::class).visible = false
                }
            }
        }

        for (childEntity in toProcess) {
            val atomWorldPos = childEntity.getAbsolutePosition()
            val difference = atomWorldPos - mouseWorld

            if (difference.length() <= radius) {
                val entityID = childEntity.getComponent(MolSelectionComponent::class)
                val entity = workState.peek().level.findByID(entityID.selectionEntity)
                if (entity != null) {
                    entity.getComponent(TransformComponent::class).visible = true
                    selection = childEntity.id
                }

                return
            }
        }

        selection = null
    }

    private fun prepareTransitionState(transAction: EditorAction) {
        transAction.execute(workState.peek().molManager, workState.peek().level)
    }

    private fun closestPointToCircleCircumference(circleCentre: Vector2f, randomPoint: Vector2f, radius: Float) : Vector2f {
        val magCentreRandomPoint = (randomPoint - circleCentre).length()
        val centreRandomPoint = (randomPoint - circleCentre)
        val position = circleCentre + (centreRandomPoint.div(magCentreRandomPoint)).mul(radius)
        return position
    }


    companion object {
        val SELECTION_RADIUS = 10.0f
        val SELECTION_MARKER_RADIUS = 25.0f
        val CONNECTION_DIST = 30.0f
    }

}
