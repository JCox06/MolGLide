package uk.co.jcox.chemvis.application.moleditorstate

import org.apache.jena.sparql.function.library.context
import org.joml.Vector2f
import org.joml.Vector3f
import uk.co.jcox.chemvis.application.graph.ChemAtom
import uk.co.jcox.chemvis.application.graph.ChemMolecule
import uk.co.jcox.chemvis.application.graph.LevelContainer
import uk.co.jcox.chemvis.application.graph.LevelRenderer
import uk.co.jcox.chemvis.application.moleditorstate.tool.AtomBondTool
import uk.co.jcox.chemvis.application.moleditorstate.tool.Tool
import uk.co.jcox.chemvis.cvengine.ApplicationState
import uk.co.jcox.chemvis.cvengine.Camera2D
import uk.co.jcox.chemvis.cvengine.ICVServices
import uk.co.jcox.chemvis.cvengine.IInputSubscriber
import uk.co.jcox.chemvis.cvengine.IRenderTargetContext
import uk.co.jcox.chemvis.cvengine.InputManager
import uk.co.jcox.chemvis.cvengine.RawInput
import java.util.UUID

class OrganicEditorState (
    val services: ICVServices,
    val renderingContext: IRenderTargetContext,
    val renderer: LevelRenderer
) : ApplicationState(renderingContext), IInputSubscriber {

    private val levelContainer = LevelContainer()
    private val camera = Camera2D(renderingContext.getWidth().toInt(), renderingContext.getHeight().toInt())

    private val currentTool: Tool = AtomBondTool()

    override fun init() {

    }

    override fun update(inputManager: InputManager, timeElapsed: Float) {

    }

    override fun render(viewport: Vector2f) {
        renderer.renderLevel(this.levelContainer, viewport)
    }

    override fun cleanup() {

    }

    override fun onPause() {
        services.inputs().unsubscribe(this)
    }

    override fun onResume() {
        services.inputs().subscribe(this)
    }

    override fun clickEvent(inputManager: InputManager, key: RawInput) {
        //So lets just add a small molecule in here to see if rendering works
        val atom = ChemAtom(Vector3f(), UUID.randomUUID(), "HELLO")
        val molecule = ChemMolecule(Vector3f(), UUID.randomUUID())
        molecule.atoms.add(atom)

        levelContainer.sceneMolecules.add(molecule)
    }

    override fun clickReleaseEvent(inputManager: InputManager, key: RawInput) {

    }

    override fun mouseMoveEvent(inputManager: InputManager, xPos: Double, yPos: Double) {

        if (inputManager.mouseClick(RawInput.MOUSE_3)) {
            val delta = inputManager.deltaMousePos()

            val scale = 0.05f

            camera.cameraPosition.add(Vector3f(-delta.x * scale, delta.y * scale, 0.0f))
        }
    }

    override fun mouseScrollEvent(inputManager: InputManager, xScroll: Double, yScroll: Double) {

    }


    fun mouseWorld() : Vector2f {
        val mousePos = renderingContext.getMousePos(services.inputs())
        return camera.screenToWorld(mousePos)
    }
}