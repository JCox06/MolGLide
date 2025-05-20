package uk.co.jcox.chemvis.application.moleditor

import org.apache.jena.sparql.function.library.leviathan.root
import org.checkerframework.checker.units.qual.mol
import org.joml.Vector3f
import org.joml.minus
import org.openscience.cdk.smiles.smarts.parser.SMARTSParserConstants.x
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
            val action = AtomCreationAction(worldPos.x, worldPos.y, "C")
            action.execute(molManager, rootNode)
        }

    }

    override fun mouseMoveEvent(inputManager: InputManager, xPos: Double, yPos: Double) {
        //Check if the current mouse position has an element
        val mouseWorldPos = camera2D.screenToWorld(inputManager.mousePos())
        selection = getClosesSelection(rootNode, Vector3f(mouseWorldPos, 0.0f), SELECTION_MARKER_RADIUS)
    }


    override fun mouseScrollEvent(inputManager: InputManager, xScroll: Double, yScroll: Double) {
        super.mouseScrollEvent(inputManager, xScroll, yScroll)
    }


    private fun getClosesSelection(level: EntityLevel, mouseWorld: Vector3f, radius: Float) : EntityLevel? {

        val positions: MutableMap<Vector3f, EntityLevel> = mutableMapOf()

        fun getChildrenPositions(child: EntityLevel) {

            if (child.hasComponent(TransformComponent::class)) {
                val trans = child.getComponent(TransformComponent::class)
                positions[Vector3f(trans.x, trans.y, trans.z)] = child
            }

            child.getChildren().forEach { child2 ->
                getChildrenPositions(child2)
            }
        }

        getChildrenPositions(level)


        positions.keys.forEach { position ->
            val diff = position - mouseWorld
            if (diff.length() >= radius) {
                val selec = positions[position]
                if (selec != null) {
                    println("Hello!")
                    selec.getComponent(TransformComponent::class).visible = true
                    return selec
                } else if (selection != null) {
//                    selection!!.getComponent(TransformComponent::class).visible = false
                }
            }
        }

        return null
    }

    companion object {
        val SELECTION_RADIUS = 10.0f
        val SELECTION_MARKER_RADIUS = 30.0f
    }
}