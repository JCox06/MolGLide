package uk.co.jcox.chemvis.application.states

import org.joml.Vector2f
import org.joml.Vector2fc
import org.joml.minus
import org.joml.plus

import uk.co.jcox.chemvis.application.ChemLevel
import uk.co.jcox.chemvis.application.ChemLevelRenderer
import uk.co.jcox.chemvis.application.chemengine.CDKManager
import uk.co.jcox.chemvis.application.chemengine.IMoleculeManager
import uk.co.jcox.chemvis.cvengine.Batch2D
import uk.co.jcox.chemvis.cvengine.BitmapFont
import uk.co.jcox.chemvis.cvengine.Camera2D
import uk.co.jcox.chemvis.cvengine.IApplicationState
import uk.co.jcox.chemvis.cvengine.IInputSubscriber
import uk.co.jcox.chemvis.cvengine.InputManager
import uk.co.jcox.chemvis.cvengine.RawInput
import uk.co.jcox.chemvis.cvengine.ShaderProgram
import java.util.UUID
import kotlin.math.floor

class EditorState(
    val batcher: Batch2D,
    val font: BitmapFont,
    val program: ShaderProgram,
    val camera: Camera2D,
) : IApplicationState, IInputSubscriber {

    private val molManager: IMoleculeManager = CDKManager()
    private val level: ChemLevel = ChemLevel()
    private val renderer: ChemLevelRenderer = ChemLevelRenderer()
    private var selectedAtom: UUID? = null
    private var bondFormationPreviewPosition: Vector2f? = null


    override fun init() {

    }

    override fun update(inputManager: InputManager) {
        bondFormationPreviewPosition = null


        if (selectedAtom != null && inputManager.mouseClick(RawInput.MOUSE_1)) {
            //We know can be sure that the user is holding down click on an atom
            //This means that the user wants to form a new bond from that atom to the next one
            createBondingLivePreview(camera.screenToWorld(inputManager.mousePos()))
        }
    }

    override fun render() {

        println(bondFormationPreviewPosition)

        if (selectedAtom != null) {
            renderer.renderSelectedAtom(level.getPosition(selectedAtom!!), SELECTION_RADIUS, program, batcher)

            if (bondFormationPreviewPosition != null) {
                renderer.renderBondFormationPreview(level.getPosition(selectedAtom!!), bondFormationPreviewPosition!!, batcher)
            }
        }

        renderer.renderLevel(level, molManager, batcher, font, program)
    }

    override fun cleanup() {

    }


    override fun clickEvent(inputManager: InputManager, key: RawInput) {
        //On every click event, first get the location clicked in the world:
        val windowClick = inputManager.mousePos()
        val worldClick: Vector2f = camera.screenToWorld(windowClick)

        if (key == RawInput.MOUSE_1) {
            newMolecule(snapToGrid(worldClick, CONNECTION_LENGTH_X, CONNECTION_LENGTH_Y))
        }

    }

    override fun mouseMoveEvent(inputManager: InputManager, xPos: Double, yPos: Double) {
        //Firstly check if we want to select atom
        if (inputManager.mouseClick(RawInput.MOUSE_1)) {
            return;
        }

        selectAtomInRange(camera.screenToWorld(inputManager.mousePos()))
    }

    private fun selectAtomInRange(mouseWorld: Vector2f) {
        molManager.allAtoms().forEach { atom ->
            val atomWorldPos = level.getPosition(atom)
            val difference = atomWorldPos - mouseWorld

            if (difference.length() <= SELECTION_MARKER_RADIUS) {
                selectedAtom = atom
                return
            }
        }

        selectedAtom = null
    }

    private fun newMolecule(worldPos: Vector2f) {
        val newMolecule = molManager.createMolecule()
        val atomInMolecule = molManager.addAtom(newMolecule, "C")
        level.addAtom(atomInMolecule, worldPos)
    }


    private fun createBondingLivePreview(mouseWorldPos: Vector2f) {
        val selectedAtom = level.getPosition(selectedAtom!!)
        val positionForNew = closestPointToCircleCircumference(selectedAtom, mouseWorldPos, CONNECTION_LENGTH_Y)
        bondFormationPreviewPosition = positionForNew
    }

    private fun closestPointToCircleCircumference(circleCentre: Vector2f, randomPoint: Vector2f, radius: Float) : Vector2f {
        val magCentreRandomPoint = (randomPoint - circleCentre).length()
        val centreRandomPoint = (randomPoint - circleCentre)
        val position = circleCentre + (centreRandomPoint.div(magCentreRandomPoint)).mul(radius)
        return position
    }

    companion object {
        private const val SELECTION_MARKER_RADIUS: Float = 30.0f
        private const val CONNECTION_LENGTH_X: Float = 25.0f
        private const val CONNECTION_LENGTH_Y: Float = 50.0f
        private const val SELECTION_RADIUS: Float = 10.0f
    }

    private fun snapToGrid(vec: Vector2fc, snapFactorX: Float, snapFactorY: Float) : Vector2f {
        val newVec: Vector2f = Vector2f()
        newVec.x = floor((vec.x() / snapFactorX) + 0.5f) * snapFactorX
        newVec.y = floor((vec.y() / snapFactorY) + 0.5f) * snapFactorY
        return newVec
    }
}