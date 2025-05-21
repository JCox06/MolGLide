package uk.co.jcox.chemvis.application.moleditor

import org.apache.jena.vocabulary.TestManifest.action
import org.checkerframework.checker.units.qual.mol
import org.joml.Vector3f
import org.joml.minus
import uk.co.jcox.chemvis.application.chemengine.CDKManager
import uk.co.jcox.chemvis.application.chemengine.IMoleculeManager
import uk.co.jcox.chemvis.cvengine.Camera2D
import uk.co.jcox.chemvis.cvengine.IApplicationState
import uk.co.jcox.chemvis.cvengine.IInputSubscriber
import uk.co.jcox.chemvis.cvengine.InputManager
import uk.co.jcox.chemvis.cvengine.LevelRenderer
import uk.co.jcox.chemvis.cvengine.RawInput
import uk.co.jcox.chemvis.cvengine.scenegraph.EntityLevel
import uk.co.jcox.chemvis.cvengine.scenegraph.TransformComponent
import kotlin.enums.enumEntries

class OrganicEditorState (
    private val levelRenderer: LevelRenderer,
    private val camera2D: Camera2D,
) : IApplicationState, IInputSubscriber {

    //Two things the OrganicEditorState requires.
    //1 - The IMoleculeManager which manages the chemical relations of the molecule
    //2 - A root EntityLevel to manage the 2D positions of the atoms of the molecule


    //At some point a file needs to exist that contains both of these data structures so workbooks can be loaded from disc
    private val molManager: IMoleculeManager = CDKManager()
    private val rootNode: EntityLevel = EntityLevel()

    var selection: EntityLevel? = null

    override fun init() {

    }

    override fun update(inputManager: InputManager, timeElapsed: Float) {
        if (selection != null) {

        }
    }

    override fun render() {
        levelRenderer.renderLevel(rootNode, camera2D)
    }

    override fun cleanup() {

    }


    override fun clickEvent(inputManager: InputManager, key: RawInput) {
        //Currently assume carbon tool selected:

        if (key == RawInput.MOUSE_1) {
            val worldPos = camera2D.screenToWorld(inputManager.mousePos())


            if (selection != null) {

                val action = AtomInsertionAction(worldPos.x, worldPos.y, "C", selection!!.parent!!)
                action.execute(molManager, rootNode)

            } else {
                val action = AtomCreationAction(worldPos.x, worldPos.y, "C")
                action.execute(molManager, rootNode)
            }

        }

    }

    override fun mouseMoveEvent(inputManager: InputManager, xPos: Double, yPos: Double) {

        if (inputManager.mouseClick(RawInput.MOUSE_1)) {
            return;
        }

        //Check if the current mouse position has an element
        val mouseWorldPos = camera2D.screenToWorld(inputManager.mousePos())
        updateSelection(rootNode, Vector3f(mouseWorldPos, 0.0f), SELECTION_MARKER_RADIUS)
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
                entityLevel.getComponent(MolSelectionComponent::class).selectionEntity.getComponent(TransformComponent::class).visible = false
            }
        }

        for (childEntity in toProcess) {
            val atomWorldPos = childEntity.getAbsolutePosition()
            val difference = atomWorldPos - mouseWorld

            if (difference.length() <= radius) {
                selection = childEntity
                selection!!.getComponent(MolSelectionComponent::class).selectionEntity.getComponent(
                    TransformComponent::class).visible = true
                return
            }
        }

        selection = null
    }


    companion object {
        val SELECTION_RADIUS = 10.0f
        val SELECTION_MARKER_RADIUS = 25.0f
        val CONNECTION_DIST = 30.0f
    }

}
