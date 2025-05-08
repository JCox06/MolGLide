package uk.co.jcox.chemvis.application.states

import org.apache.jena.sparql.pfunction.library.listLength
import org.apache.jena.vocabulary.AS.radius
import org.checkerframework.checker.units.qual.mol
import org.joml.Vector2f
import org.joml.Vector3f
import org.joml.Vector3fc
import org.joml.Vector4f
import org.joml.minus
import org.joml.plus
import org.lwjgl.opengl.GL11
import org.openscience.cdk.AtomRef
import org.xmlcml.euclid.Vector2

import uk.co.jcox.chemvis.application.ChemLevel
import uk.co.jcox.chemvis.application.chemengine.CDKManager
import uk.co.jcox.chemvis.application.chemengine.IMoleculeManager
import uk.co.jcox.chemvis.cvengine.Batch2D
import uk.co.jcox.chemvis.cvengine.BitmapFont
import uk.co.jcox.chemvis.cvengine.Camera2D
import uk.co.jcox.chemvis.cvengine.IApplicationState
import uk.co.jcox.chemvis.cvengine.IInputSubscriber
import uk.co.jcox.chemvis.cvengine.InputManager
import uk.co.jcox.chemvis.cvengine.Mesh
import uk.co.jcox.chemvis.cvengine.RawInput
import uk.co.jcox.chemvis.cvengine.ShaderProgram
import uk.co.jcox.chemvis.cvengine.Shaper2D
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

    private var selectedAtom: UUID? = null

    private var startAction: Boolean = false

    override fun init() {

    }

    override fun update(inputManager: InputManager) {
        if (inputManager.mouseClick(RawInput.MOUSE_1)) {
            startAction = true
        } else {
            startAction = false
        }

        val mousePos = inputManager.mousePos()
        val mouseWorldPos = camera.screenToWorld(Vector4f(mousePos.x, mousePos.y, 0.0f, 1.0f))

        createStateForLivePreview(Vector2f(mouseWorldPos.x, mouseWorldPos.y))
    }

    private fun createStateForLivePreview(mouseWorldPos: Vector2f) {
        if (startAction && selectedAtom != null) {
            //We know the user is holding down click AND an atom has been selected
            //Assuming we have 45 degrees of free rotation, find that rotation

            //todo - Actually instead of quantising, let's just define a GRID for the editor
            //Atoms have to exist on the grid = Easy Quantisation
            val posMol = level.getPosition(selectedAtom!!)
            val position = closestPointToCircleCircumfrance(Vector2f(posMol.x, posMol.y), mouseWorldPos, CONNECTION_LENGTH)
            batcher.begin(GL11.GL_TRIANGLES)

            val newPos = snapToGrid(Vector3f(position.x, position.y, 0.0f))

            batcher.addBatch(Shaper2D.rectangle(newPos.x, newPos.y, 5.0f, 5.0f))

            batcher.end()
        }
    }

    private fun closestPointToCircleCircumfrance(circleCentre: Vector2f, randomPoint: Vector2f, radius: Float) : Vector2f {
        val magCentreRandomPoint = (randomPoint - circleCentre).length()
        val centreRandomPoint = (randomPoint - circleCentre)
        val position = circleCentre + (centreRandomPoint.div(magCentreRandomPoint)).mul(radius)
        return position
    }

    override fun render() {

        //1) See if any atom is currently selected
        if (selectedAtom != null) {
            val position = level.getPosition(selectedAtom!!)

            program.uniform("textureMode", 0);

            if (startAction) {
                program.uniform("fontColour", Vector3f(0.02f, 0.02f, 0.02f))

            } else {
                program.uniform("fontColour", Vector3f(0.05f, 0.05f, 0.05f))
            }

            batcher.begin(GL11.GL_TRIANGLE_FAN)
            batcher.addBatch(Shaper2D.circle(position.x, position.y, SELECTION_RADIUS))
            batcher.end()

            program.uniform("textureMode", 1)
        }

        //2) Go through all the molecules and then through all the atoms
        font.blend = true
        molManager.molecules().forEach { mol ->
            molManager.relatedAtoms(mol).forEach { atom ->
                val pos = level.getPosition(atom)
                font.text(molManager.getAtomSymbol(atom), batcher, program, pos.x - (font.glyphSize() / 2), pos.y - (font.glyphSize() /2))
            }
        }


        //3) todo - Important, Add a live preview
        //When creating a new bond, add a live preview what is happening, as it happens
        //This doesn't change the level, as all the logic is in the last bit of rendering
        //However, everything about the state is stored so when the RELEASE callback is sent from the engine InputManager
        //We can take everything we have here, and sent the required info to the MolManager and the Level
        //Live preview logic is done in the #update method, live preview rendering done below here!

    }

    override fun cleanup() {

    }

    override fun clickEvent(inputManager: InputManager, key: RawInput) {

        val windowCoordinateClicked = inputManager.mousePos()
        val worldSpaceCoordinate = camera.screenToWorld(Vector4f(windowCoordinateClicked, 0.0f, 1.0f))

        //Add a molecule by clicking somewhere in the editor
        if (key == RawInput.MOUSE_1 && selectedAtom == null) {
            //Create a new molecule, for now this is "molecule will just be a single carbon, but later it will depend on the tool"
            val molecule = molManager.createMolecule()
            val atomInMolecule = molManager.addAtom(molecule, "C")
            //Now the molecule itself is created, but it needs a physical representation, otherwise it will not render
            //The "ChemLevel" object manages the positions of atoms, bonds, molecules ect. So we need to get the position of our molecule in the world
            //Add the atom to the level
            level.addAtom(atomInMolecule, snapToGrid(Vector3f(worldSpaceCoordinate.x, worldSpaceCoordinate.y, worldSpaceCoordinate.z)))
            return;
        }

        if (key == RawInput.MOUSE_1) {
            //Now we are here, we can assume that the mouse is hovering over an atom
            //In that case the tool selected will add to an existing molecule, instead of creating a new one!
            //Based on the current tool, add the atom group, and make a bond from the selected atom, to the new atom!
            val molecule = molManager.relatedMolecule(selectedAtom)
            val newAtom = molManager.addAtom(molecule, "C")
            molManager.formBond(molecule, selectedAtom, newAtom, 1)
            level.addAtom(newAtom, Vector3f(level.getPosition(selectedAtom!!).x + CONNECTION_LENGTH, level.getPosition(selectedAtom!!).y, level.getPosition(selectedAtom!!).z))
        }
    }

    override fun mouseMoveEvent(inputManager: InputManager, xPos: Double, yPos: Double) {

        //We do not want to select a new atom if the mouse main click is held down
        if (! inputManager.mouseClick(RawInput.MOUSE_1)) {
            val worldSpaceCoordinates = camera.screenToWorld(Vector4f(xPos.toFloat(), yPos.toFloat(), 0.0f, 1.0f))
            selectedAtom = getSelectedAtom(Vector3f(worldSpaceCoordinates.x, worldSpaceCoordinates.y, worldSpaceCoordinates.z))
        }
    }

    private fun getSelectedAtom(mouseWorldPos: Vector3f): UUID? {
        //Check through all the atoms
        molManager.allAtoms().forEach { atom ->
            val atomWorldPos = level.getPosition(atom)

            val difference = atomWorldPos - mouseWorldPos

            if (difference.length() <= SELECTION_MARKER_RADIUS) {
                return atom
            }
        }
        return null
    }

    companion object {
        private const val SELECTION_MARKER_RADIUS: Float = 30.0f
        private const val CONNECTION_LENGTH: Float = 50.0f
        private const val SELECTION_RADIUS: Float = 10.0f
    }

    private fun snapToGrid(vec: Vector3fc) : Vector3f {
        val newVec: Vector3f = Vector3f()
        newVec.x = floor((vec.x() / CONNECTION_LENGTH) + 0.5f) * CONNECTION_LENGTH
        newVec.y = floor((vec.y() / CONNECTION_LENGTH) + 0.5f) * CONNECTION_LENGTH
        newVec.z = vec.z()

        return newVec
    }
}