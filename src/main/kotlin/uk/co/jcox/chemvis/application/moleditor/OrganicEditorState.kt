package uk.co.jcox.chemvis.application.moleditor

import imgui.ImGui
import imgui.type.ImBoolean
import imgui.type.ImInt
import org.joml.Vector2f
import org.joml.Vector3f
import org.joml.minus
import uk.co.jcox.chemvis.application.chemengine.CDKManager
import uk.co.jcox.chemvis.application.moleditor.actions.AtomCreationAction
import uk.co.jcox.chemvis.application.moleditor.actions.AtomInsertionAction
import uk.co.jcox.chemvis.application.moleditor.actions.AtomInsertionInlineAction
import uk.co.jcox.chemvis.application.moleditor.actions.EditorAction
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
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

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
    var debugUI = true
    val imGuiTool = ImInt(1)
    val imGuiMode = ImInt(0)
    val imGuiImplicitCarbon = ImBoolean(true)
    val imGuiImplicitHydrogen = ImBoolean(true)
    var showInlineAddMenu: Boolean = false
    var selectedAnchor: EntityLevel? = null
    var implicitCycle = false

    var mainMode = true

    override fun init() {
        workState.push(ChemLevelPair(EntityLevel(), CDKManager()))
    }

    override fun update(inputManager: InputManager, timeElapsed: Float) {

        selection?.let{
            val selecEntity = workState.peek().level.findByID(it)
            val molEntity = selecEntity?.parent
            moleculeUIID = molEntity?.getComponent(MolIDComponent::class)?.molID
        }

    }

    override fun render(viewport: Vector2f) {
        levelRenderer.renderLevel(workState.peek().level, camera2D, viewport)

        if (debugUI) {
            drawDebugUI()
        }

        if (showInlineAddMenu) {
            drawInlineAddMenu()
        }
    }



    override fun cleanup() {

    }


    override fun clickEvent(inputManager: InputManager, key: RawInput) {
        //If the user clicks LCTRL, then we can do some actions
        if (inputManager.keyClick(RawInput.LCTRL)) {
            inputControlActions(key)
        }

        //Currently assume carbon tool selected:
        if (key == RawInput.MOUSE_1 && mainMode) {
            inputOrganicInfo(inputManager)
        }

        if (key == RawInput.MOUSE_1 && !mainMode) {
            inputInlineModeInfo(inputManager)
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

        var element = AtomInsert.CARBON

        if (imGuiTool.get() == 1) {
           element = AtomInsert.CARBON
        } else if (imGuiTool.get() == 2) {
            element = AtomInsert.HYDROGEN
        } else if (imGuiTool.get() == 3) {
            element = AtomInsert.CHLORINE
        } else if (imGuiTool.get() == 4) {
            element = AtomInsert.OXYGEN
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
            val action = AtomInsertionAction(circlePos.x, circlePos.y, element, selectedEntity.parent, selectedEntity, imGuiImplicitCarbon.get())
            prepareTransitionState(action)
            draggedEntity = action.insertedAtom

            //If the selection does not exist, then create a new atom/molecule
        } ?: run {
            val action = AtomCreationAction(worldPos.x, worldPos.y, element, imGuiImplicitHydrogen.get())
            prepareTransitionState(action)
        }

        setImplicitCarbons(implicitCycle)
    }


    private fun inputInlineModeInfo(inputManager: InputManager) {
        val mouseWorldPos = camera2D.screenToWorld(inputManager.mousePos())
        val minDist = 2.0f

        workState.peek().level.traverseFunc {

            if (it.hasComponent(AnchorComponent::class)) {
                val absPos = it.getAbsolutePosition()
                val mouseAbsPosDiff = absPos - Vector3f(mouseWorldPos, XY_PLANE)
                if (mouseAbsPosDiff.length() <= minDist) {
                    showInlineAddMenu = true
                    selectedAnchor = it
                }
            }
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
        if (mainMode) {
            val mouseWorldPos = camera2D.screenToWorld(inputManager.mousePos())
            updateSelection(workState.peek().level, Vector3f(mouseWorldPos, 0.0f), SELECTION_MARKER_RADIUS)
        }

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
        transAction.runAction(workState.peek().molManager, workState.peek().level)
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


    private fun switchToInlineBondMode() {
        mainMode = false

        //Go through all the Anchors and show them

        workState.peek().level.traverseFunc {
            if (it.hasComponent(AnchorComponent::class) && it.hasComponent(TransformComponent::class)) {
                it.getComponent(TransformComponent::class).visible = true
            }
        }
    }


    private fun leaveInlineBondMode() {
        mainMode = true

        //Go through all the Anchors and hide them
        workState.peek().level.traverseFunc {
            if (it.hasComponent(AnchorComponent::class) && it.hasComponent(TransformComponent::class)) {
                it.getComponent(TransformComponent::class).visible = false
            }
        }
    }

    private fun drawDebugUI() {
        ImGui.begin("Global Menu")


        ImGui.separatorText("Debug Info")

        ImGui.textWrapped("Dragged Entity $draggedEntity")

        ImGui.textWrapped("Selected Entity $selection")

        ImGui.textWrapped("WorkState Level: ${workState.size}")

        if (ImGui.button("Undo")) {
            workState.pop()
        }


        ImGui.separatorText("Implicit Options")

        ImGui.checkbox("Create Implicit Hydrogens", imGuiImplicitHydrogen)
        if (ImGui.isItemHovered()) {
            ImGui.setTooltip("Automatically add hydrogen to carbon to fill the octet")
        }

        if (ImGui.button("Toggle Carbon View")) {
            implicitCycle = !implicitCycle
            setImplicitCarbons(implicitCycle)
        }

        ImGui.separatorText("Tools")


        if (ImGui.radioButton("Atom Bond Mode", imGuiMode, 0)) {
            leaveInlineBondMode()
        }

        ImGui.sameLine()


        if (ImGui.radioButton("Inline Bond Mode", imGuiMode, 1)) {
            switchToInlineBondMode()
        }

        ImGui.beginDisabled(imGuiMode.get() != 0)

        ImGui.radioButton("Carbon Tool", imGuiTool, 1); ImGui.sameLine()
        ImGui.radioButton("Hydrogen Tool", imGuiTool, 2); ImGui.sameLine()
        ImGui.radioButton("Chlorine Tool", imGuiTool, 3); ImGui.sameLine()
        ImGui.radioButton("Oxygen Tool", imGuiTool, 4)

        ImGui.endDisabled()



        if (imGuiMode.get() == 1) {
            ImGui.text("Inline Bond mode is active!")
        }


        ImGui.separatorText("CURRENT SELECTION DETAILS")
        if (selection != null && moleculeUIID != null) {
            ImGui.separatorText("Molecule")
            ImGui.text("Molecular Formula ${workState.peek().molManager.getMolecularFormula(moleculeUIID)}")

        }

        ImGui.end()
    }


    private fun drawInlineAddMenu() {
        ImGui.begin("Add atom inline")
        ImGui.text("Choose a group to add:")

        if (ImGui.button("H")) {
          selectedAnchor?.let {
              triggerInlineAction(it, AtomInsert.HYDROGEN)
          }
        };

        ImGui.separatorText("EXIT")
        if (ImGui.button("Close Menu")) {
            showInlineAddMenu = false
        }

        ImGui.end()
    }


    private fun triggerInlineAction(anchorEntity: EntityLevel, element: AtomInsert) {
        val action = AtomInsertionInlineAction(anchorEntity, element)
        prepareTransitionState(action)
    }


    private fun setImplicitCarbons(implicit: Boolean) {
        workState.peek().level.traverseFunc {
            if (it.hasComponent(MolIDComponent::class) && it.hasComponent(TransformComponent::class) && it.hasComponent(AtomComponent::class) && !it.hasComponent(AlwaysExplicit::class)) {
                val atomID = it.getComponent(MolIDComponent::class)
                val trans = it.getComponent(TransformComponent::class)
                val mol = it.parent

                if (mol != null) {
                    val mols = mol.getComponent(MolIDComponent::class)
                    if (workState.peek().molManager.isOfElement(mols.molID, atomID.molID, "C")) {
                        val bonds = workState.peek().molManager.getBonds(mols.molID, atomID.molID)
                        if (bonds >= CARBON_IMPLICIT_LIMIT) {
                            trans.visible = implicit
                        }
                    }
                }
            }
        }
    }

    companion object {
        const val SELECTION_RADIUS = 8.0f
        const val SELECTION_MARKER_RADIUS = 15.0f
        const val CONNECTION_DIST = 40.0f
        const val INLINE_DIST = 10.0f

        const val CARBON_IMPLICIT_LIMIT = 4

        const val XY_PLANE = -1.0f
    }

}
