package uk.co.jcox.chemvis.application.moleditorstate.tool

import org.apache.jena.sparql.function.library.e
import org.joml.Vector2f
import org.joml.Vector3f
import org.joml.minus
import org.joml.plus
import org.xmlcml.euclid.Vector2
import uk.co.jcox.chemvis.application.graph.ChemAtom
import uk.co.jcox.chemvis.application.graph.ChemMolecule
import uk.co.jcox.chemvis.application.graph.LevelContainer
import uk.co.jcox.chemvis.application.moleditorstate.ActionManager
import uk.co.jcox.chemvis.application.moleditorstate.OrganicEditorState
import uk.co.jcox.chemvis.application.moleditorstate.SelectionManager
import uk.co.jcox.chemvis.application.moleditorstate.action.AtomCreationAction
import uk.co.jcox.chemvis.application.moleditorstate.action.AtomInsertionAction
import uk.co.jcox.chemvis.cvengine.Camera2D
import uk.co.jcox.chemvis.cvengine.IRenderTargetContext
import uk.co.jcox.chemvis.cvengine.IResourceManager
import uk.co.jcox.chemvis.cvengine.InputManager

class AtomBondTool (
    toolboxContext: ToolboxContext, renderingContext: IRenderTargetContext, inputManager: InputManager, camera2D: Camera2D, levelContainer: LevelContainer, selectionManager: SelectionManager,
    actionManager: ActionManager,
) : Tool(toolboxContext, renderingContext, inputManager, camera2D, levelContainer, selectionManager, actionManager){

    var toolMode: Mode = Mode.None

    override fun onClick(clickX: Float, clickY: Float) {
        toolMode = prepareCorrectMode(clickX, clickY)

        when (val mode = toolMode) {
            is Mode.None -> {}
            is Mode.MolCreation -> createNewMolecule(mode)
            is Mode.AtomInsertion -> addAtomToMolecule(mode)

            is Mode.AtomInsertionDragging -> {} //Not an action that takes place here
        }
    }

    override fun onRelease(clickX: Float, clickY: Float) {
        //On release reset anything that is going on
        //This ensures transient events will all end on release of a key
        toolMode = Mode.None
    }


    override fun renderTransients(resourceManager: IResourceManager) {
        renderTransientSelectionMarker(resourceManager)
    }

    private fun createNewMolecule(molCreation: Mode.MolCreation) {
        val atomCreationAction = AtomCreationAction(molCreation.xPos, molCreation.yPos, toolboxContext.atomInsert)
        actionManager.executeAction(atomCreationAction)
    }

    private fun addAtomToMolecule(molInsertion: Mode.AtomInsertion) {
        val atomInsertionAction = AtomInsertionAction(toolboxContext.atomInsert, molInsertion.srcAtom)
        actionManager.executeAction(atomInsertionAction)

        //While the user is pressing and holding, we need to change the mode to dragging.
        //Dragging mode is finalised upon key release

        atomInsertionAction.newLevelAtom?.let {
            toolMode = Mode.AtomInsertionDragging(molInsertion.srcAtom, it)
        }
    }


    private fun prepareCorrectMode(clickX: Float, clickY: Float) : Mode {
        val selectionType = selectionManager.primarySelection
        if (selectionType is SelectionManager.Type.None) {
            //No molecule is selected, therefore we can't add to a molecule, and instead
            //we can only create a new one
            return Mode.MolCreation(clickX, clickY)
        }

        if (selectionType is SelectionManager.Type.Active) {
            return Mode.AtomInsertion(selectionType.atom)
        }

        return Mode.None
    }

    override fun update() {
        val currentMode = toolMode
        if (currentMode is Mode.AtomInsertionDragging) {
            //Drag the atom around in a circle near the mouse
            handleNewAtomDragging(currentMode)

            //Check if the implicit hydrogen group needs to be put on the left of the atom
            autoMoveHydrogenGroup(currentMode)
        }
    }

    private fun handleNewAtomDragging(mode: Mode.AtomInsertionDragging) {
        val mousePos = mouseWorld()
        val srcPos = mode.srcAtom.getWorldPosition()

        val newAtomPos = closestPointToCircleCircumference(Vector2f(srcPos.x, srcPos.y), mousePos, OrganicEditorState.CONNECTION_DISTANCE)

        val localTransform = mode.srcAtom.parent.localPos
        val localAtomTransform = Vector3f(newAtomPos.x, newAtomPos.y, 0.0f) - localTransform
        mode.destAtom.localPos.x = localAtomTransform.x
        mode.destAtom.localPos.y = localAtomTransform.y
    }


    private fun autoMoveHydrogenGroup(mode: Mode.AtomInsertionDragging) {
        val dragging = mode.destAtom
        val stationary = mode.srcAtom

        autoMoveGroup(dragging, stationary)
        autoMoveGroup(stationary, dragging)
    }

    /**
     * Given an atom to change, depending on its position to another atom
     */
    private fun autoMoveGroup(checkAgainst: ChemAtom, applier: ChemAtom) {
        val leftTest = ChemAtom.RelationalPos.LEFT.mod + applier.getWorldPosition()
        val rightTest = ChemAtom.RelationalPos.RIGHT.mod + applier.getWorldPosition()

        val leftDistance = checkAgainst.getWorldPosition().distance(leftTest)
        val rightDistance = checkAgainst.getWorldPosition().distance(rightTest)

        //If they are the same, then the hydrogen does not need to move
        if (leftDistance == rightDistance) {
            return
        }

        //Otherwise move the implicit hydrogen to the side that is further away
        //Hence more space and less crowded
        if (rightDistance > leftDistance) {
            applier.implicitHydrogenPos = ChemAtom.RelationalPos.RIGHT
        } else {
            applier.implicitHydrogenPos = ChemAtom.RelationalPos.LEFT
        }
    }

    sealed class Mode {

        object None: Mode()
        class MolCreation(val xPos: Float, val yPos: Float): Mode()


        /**
         * Used to represent the moment when a new atom is added to an existing molecule and a bond is formed
         * @property srcAtom the existing atom that the new atom will bond to
         */
        class AtomInsertion(val srcAtom: ChemAtom): Mode()

        /**
         * Used to represent the period of time after the insertion of a new atom, while the user is holding
         * the mouse button down
         * @property srcAtom the original atom
         * @property destAtom the atom that was just created by the AtomInsertionAction class
         */
        class AtomInsertionDragging(val srcAtom: ChemAtom, val destAtom: ChemAtom) : Mode()
    }
}