package uk.co.jcox.chemvis.application.moleditor

import imgui.ImGui
import imgui.type.ImInt
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
    var draggedEntity: UUID? = null
    var moleculeUIID: UUID? = null
    var debugUI = false

    val imGuiTool = ImInt(0)
    val imGuiMode = ImInt(0)

    override fun init() {
        workState.push(ChemLevelPair(EntityLevel(), CDKManager()))
    }

    override fun update(inputManager: InputManager, timeElapsed: Float) {
    }

    override fun render() {
        levelRenderer.renderLevel(workState.peek().level, camera2D)

        if (debugUI) {
            drawDebugUI()
        }

        if (moleculeUIID != null) {
            drawMoleculeLookup()
        }
    }

    override fun cleanup() {

    }


    override fun clickEvent(inputManager: InputManager, key: RawInput) {
        //If the user clicks LCTRL, then we can do some actions
        if (inputManager.keyClick(RawInput.LCTRL)) {
            inputControlActions(key)
        }


        if (key == RawInput.MOUSE_2) {
            selection?.let{
                val selecEntity = workState.peek().level.findByID(it)
                val molEntity = selecEntity?.parent
                moleculeUIID = molEntity?.getComponent(MolIDComponent::class)?.molID
            }
        }

        //Currently assume carbon tool selected:
        if (key == RawInput.MOUSE_1) {
            inputOrganicInfo(inputManager)
        }
    }


    private fun inputControlActions(key: RawInput) {
        if (key == RawInput.KEY_F) {
            workState.add(workState.peek().clone())
        }

        if (key == RawInput.KEY_Z) {
            if (workState.size > 1) {
                workState.pop()
            }
        }

        if (key == RawInput.KEY_L) {
            debugUI = !debugUI
        }
    }

    private fun inputOrganicInfo(inputManager: InputManager) {
        workState.push(workState.peek().clone())

        val worldPos = camera2D.screenToWorld(inputManager.mousePos())

        var element = "C"

        if (imGuiTool.get() == 0) {
           element = "C"
        } else if (imGuiTool.get() == 1) {
            element = "H"
        } else if (imGuiTool.get() == 2) {
            element = "Cl"
        }

        //If selection exists, add the new atom to the selected molecule
        selection?.let {
            val selectedEntity = workState.peek().level.findByID(it)
            val transformAtom = selectedEntity?.getAbsolutePosition()
            val parent = selectedEntity?.parent
            if (transformAtom == null || parent == null) {
                return
            }

            val circlePos = closestPointToCircleCircumference(Vector2f(transformAtom.x, transformAtom.y), worldPos, CONNECTION_DIST)
            val action = AtomInsertionAction(circlePos.x, circlePos.y, element, selectedEntity.parent, selectedEntity)
            prepareTransitionState(action)
            draggedEntity = action.insertedAtom

            //If the selection does not exist, then create a new atom/molecule
        } ?: run {
            val action = AtomCreationAction(worldPos.x, worldPos.y, element)
            prepareTransitionState(action)
        }
    }


    override fun clickReleaseEvent(inputManager: InputManager, key: RawInput) {
        if (key == RawInput.MOUSE_1) {
            draggedEntity = null
        }
    }

    override fun mouseMoveEvent(inputManager: InputManager, xPos: Double, yPos: Double) {

        val dragging = draggedEntity
        val selecting = selection
        if (dragging != null && selecting != null) {
            inputTransientDragAndDropAtomUI(inputManager, dragging, selecting)
        }

        if (inputManager.mouseClick(RawInput.MOUSE_1)) {
            return
        }

        //Check if the current mouse position has an element
        val mouseWorldPos = camera2D.screenToWorld(inputManager.mousePos())
        updateSelection(workState.peek().level, Vector3f(mouseWorldPos, 0.0f), SELECTION_MARKER_RADIUS)

    }



    private fun inputTransientDragAndDropAtomUI(inputManager: InputManager, draggingEntity: UUID, selectedEntity: UUID) {
        //Handles the drag and drop behaviour when adding an atom to an existing molecule

        val entity = workState.peek().level.findByID(draggingEntity)
        val parent = entity?.parent
        val parentAbs = parent?.getAbsolutePosition()

        val selectedEntity = workState.peek().level.findByID(selectedEntity)
        //Get position of the molecule
        val transformMolecule = selectedEntity?.getAbsolutePosition()

        val mouseWorld = camera2D.screenToWorld(inputManager.mousePos())

        if (transformMolecule != null && parentAbs != null) {
            val draggedPosition = closestPointToCircleCircumference(Vector2f(transformMolecule.x, transformMolecule.y), mouseWorld, CONNECTION_DIST)

            val entityTransform = entity.getComponent(TransformComponent::class)
            entityTransform.x = draggedPosition.x - parentAbs.x
            entityTransform.y = draggedPosition.y - parentAbs.y
        }
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


    private fun drawDebugUI() {
        ImGui.begin("Debug UI")


        ImGui.separatorText("Debug Info")

        ImGui.textWrapped("Dragged Entity $draggedEntity")

        ImGui.textWrapped("Selected Entity $selection")

        ImGui.textWrapped("WorkState Level: ${workState.size}")

        if (ImGui.button("Undo")) {
            workState.pop()
        }

        ImGui.separatorText("Tools")


        ImGui.radioButton("Atom Bond Mode", imGuiMode, 0); ImGui.sameLine()
        ImGui.radioButton("Inline Bond Mode", imGuiMode, 1)

        ImGui.beginDisabled(imGuiMode.get() != 0)

        ImGui.radioButton("Carbon Tool", imGuiTool, 0); ImGui.sameLine()
        ImGui.radioButton("Hydrogen Tool", imGuiTool, 1); ImGui.sameLine()
        ImGui.radioButton("Chlorine Tool", imGuiTool, 2);

        ImGui.endDisabled()



        if (imGuiMode.get() == 1) {
            ImGui.text("Inline Bond mode is active!")
        }

        ImGui.end()
    }

    private fun drawMoleculeLookup() {
        ImGui.begin("Molecule Lookup")

        ImGui.text("Molecule Manager ID ${this.moleculeUIID}")

        ImGui.text("Formula: ${workState.peek().molManager.getMolecularFormula(this.moleculeUIID)}")

        if (ImGui.button("Close Window")) {
            this.moleculeUIID = null
        }

        ImGui.end()
    }

    companion object {
        const val SELECTION_RADIUS = 10.0f
        const val SELECTION_MARKER_RADIUS = 25.0f
        const val CONNECTION_DIST = 30.0f
    }

}
