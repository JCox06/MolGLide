package uk.co.jcox.chemvis.application.moleditorstate.tool

import org.apache.jena.sparql.pfunction.library.container
import org.joml.Vector2f
import org.joml.Vector3f
import org.joml.minus
import org.joml.plus
import uk.co.jcox.chemvis.application.graph.ChemAtom
import uk.co.jcox.chemvis.application.graph.LevelContainer
import uk.co.jcox.chemvis.application.moleditorstate.ActionManager
import uk.co.jcox.chemvis.application.moleditorstate.AtomInsert
import uk.co.jcox.chemvis.application.moleditorstate.OrganicEditorState
import uk.co.jcox.chemvis.application.moleditorstate.SelectionManager
import uk.co.jcox.chemvis.application.moleditorstate.StereoChem
import uk.co.jcox.chemvis.application.moleditorstate.action.AtomCreationAction
import uk.co.jcox.chemvis.application.moleditorstate.action.AtomInsertionAction
import uk.co.jcox.chemvis.application.moleditorstate.action.AtomReplacementAction
import uk.co.jcox.chemvis.application.moleditorstate.action.ChangeStereoAction
import uk.co.jcox.chemvis.application.moleditorstate.action.IncrementBondOrderAction
import uk.co.jcox.chemvis.application.moleditorstate.action.RingCyclisationAction
import uk.co.jcox.chemvis.application.ui.tool.AtomBondToolView
import uk.co.jcox.chemvis.cvengine.Camera2D
import uk.co.jcox.chemvis.cvengine.IRenderTargetContext
import uk.co.jcox.chemvis.cvengine.IResourceManager
import uk.co.jcox.chemvis.cvengine.InputManager

class AtomBondTool(
    toolViewUI: AtomBondToolView,
    renderingContext: IRenderTargetContext,
    inputManager: InputManager,
    camera2D: Camera2D,
    levelContainer: LevelContainer,
    selectionManager: SelectionManager,
    actionManager: ActionManager,
) : Tool<AtomBondToolView>(toolViewUI, renderingContext, inputManager, camera2D, levelContainer, selectionManager, actionManager) {

    var toolMode: Mode = Mode.None

    override fun onClick(clickX: Float, clickY: Float) {
        toolMode = prepareCorrectMode(clickX, clickY)

        when (val mode = toolMode) {
            is Mode.None -> {}
            is Mode.MolCreation -> createNewMolecule(mode)
            is Mode.AtomInsertion -> addAtomToMolecule(mode)

            is Mode.AtomInsertionDragging -> {} //Not an action that takes place here
            is Mode.PostReplacement -> {} //Not an action that happens here
        }
    }

    override fun onRelease(clickX: Float, clickY: Float) {
        //On release reset anything that is going on
        //This ensures transient events will all end on release of a key
        toolMode = Mode.None
    }


    override fun renderTransients(resourceManager: IResourceManager) {
        val primarySelection = selectionManager.primarySelection
        if (primarySelection is SelectionManager.Type.Active) {
            renderTransientSelectionMarker(resourceManager, primarySelection.atom)
        }
    }

    private fun createNewMolecule(molCreation: Mode.MolCreation) {
        val atomCreationAction = AtomCreationAction(molCreation.xPos, molCreation.yPos, toolViewUI.getInsert())
        actionManager.executeAction(atomCreationAction)
    }

    private fun addAtomToMolecule(molInsertion: Mode.AtomInsertion) {
        val atomReplacementAction = AtomReplacementAction(molInsertion.srcAtom, toolViewUI.getInsert())
        actionManager.executeAction(atomReplacementAction)
        toolMode = Mode.PostReplacement(molInsertion.srcAtom)
    }


    private fun prepareCorrectMode(clickX: Float, clickY: Float): Mode {
        val selectionType = selectionManager.primarySelection
        if (selectionType is SelectionManager.Type.None) {
            //No molecule is selected, therefore we can't add to a molecule, and instead
            //we can only create a new one
            return Mode.MolCreation(clickX, clickY)
        }

        if (selectionType is SelectionManager.Type.Active) {
            return Mode.AtomInsertion(selectionType.atom,)
        }

        return Mode.None
    }

    override fun update() {
        val currentMode = toolMode

        //Check if we need to turn an atom replacement action into an atom insertion action
        if (currentMode is Mode.PostReplacement) {
            convertPostReplaceToAtomInsert(currentMode)
        }

        if (currentMode is Mode.AtomInsertionDragging) {
            //Drag the atom around in a circle near the mouse
            handleNewAtomDragging(currentMode)

            //Check if the implicit hydrogen group needs to be put on the left of the atom
            autoMoveHydrogenGroup(currentMode)

            //Check if a double bond (todo: or triple bond) needs to be formed
            checkBondOrderChange(currentMode)

        }
    }


    private fun handleNewAtomDragging(mode: Mode.AtomInsertionDragging) {
        val mousePos = mouseWorld()
        val srcPos = mode.srcAtom.getWorldPosition()

        val newAtomPos = closestPointToCircleCircumference(
            Vector2f(srcPos.x, srcPos.y),
            mousePos,
            OrganicEditorState.CONNECTION_DISTANCE
        )

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
     * @param checkAgainst the atom that is not affected by this function
     * @param applier the atom where the implicit group is changed, depending on the tests from the checkAgainst
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

    /**
     * When in dragging mode, the user is free to move the newly inserted atom wherever they want
     * Should this atom align with an already-existing bond, then, attempt to increase the bond order of that bond
     *
     * If the user pulls away again, the action needs to be undone - by using the CommandExecutor,
     * but also, this tool needs to be restored to allow continued movement of the new atom.
     *
     * This action is mainly used for detecting bond order changes, but can also be used to detect cyclisation
     */
    private fun checkBondOrderChange(draggingMode: Mode.AtomInsertionDragging) {
        val draggingPos = draggingMode.destAtom.getWorldPosition()

        //todo At the moment this only works for atoms in the same molecule
        val commonMolecule = draggingMode.destAtom.parent

        //Note the shallow copy of toList(). This is because we don't know if undoing the last action could change the list

        val atom = commonMolecule.atoms.toList().find {
            draggingPos.equals(
                it.getWorldPosition(),
                0.25f
            ) && it != draggingMode.destAtom
        }

        if (atom != null && draggingMode.allowBondChanges) {

            actionManager.undoLastAction()

            //Now we need to decide if we want to form a bond of a higher order
            //or form a cyclisation

            //If a bond exists between these two atoms, then increase bond order
            //If no bond exists, a ring is forming

            draggingMode.allowBondChanges = false


            val bond = commonMolecule.bonds.find {
                (it.atomA == draggingMode.srcAtom && it.atomB == atom) || (it.atomA == atom && it.atomB === draggingMode.srcAtom)
            }

            if (bond == null) {
                //Form a ring
                val action = RingCyclisationAction(commonMolecule, draggingMode.srcAtom, atom)
                actionManager.executeAction(action)
            } else if (levelContainer.chemManager.getStereoChem(bond.molManagerLink) == toolViewUI.stereoChem) {
                val action = IncrementBondOrderAction(commonMolecule, bond)
                actionManager.executeAction(action)
                if (bond.atomA == draggingMode.srcAtom) {
                    bond.flipDoubleBond = true
                } else {
                    bond.flipDoubleBond = false
                }
            } else {
                val action = ChangeStereoAction(bond, toolViewUI.stereoChem)
                actionManager.executeAction(action)
            }
        }

        if (atom == null && !draggingMode.allowBondChanges) {
            actionManager.undoLastAction()
//            actionManager.executeAction(draggingMode.restore)

            addAtomToMolecule(Mode.AtomInsertion(draggingMode.srcAtom))
            draggingMode.allowBondChanges = true
        }
    }


    private fun convertPostReplaceToAtomInsert(mode: Mode.PostReplacement) {
        val mouseWorld = mouseWorld()
        val dMouse = inputManager.deltaMousePos()

        if (dMouse.length() >= SIG_DELTA) {
            //Uno the atom replace action
            actionManager.undoLastAction()

            val atomInsertionAction = AtomInsertionAction(toolViewUI.getInsert(), toolViewUI.stereoChem, mode.srcAtom)
            actionManager.executeAction(atomInsertionAction)

            atomInsertionAction.newLevelAtom?.let {
                toolMode = Mode.AtomInsertionDragging(mode.srcAtom, it, true)
            }
        }
    }


    sealed class Mode {

        object None : Mode()
        class MolCreation(val xPos: Float, val yPos: Float) : Mode()


        /**
         * Used to represent the moment when a new atom is added to an existing molecule and a bond is formed
         * @property srcAtom the existing atom that the new atom will bond to
         */
        class AtomInsertion(val srcAtom: ChemAtom) : Mode()

        /**
         * Used to represent the period of time after the insertion of a new atom, while the user is holding
         * the mouse button down
         * @property srcAtom the original atom
         * @property destAtom the atom that was just created by the AtomInsertionAction class
         */
        class AtomInsertionDragging(val srcAtom: ChemAtom, val destAtom: ChemAtom, var allowBondChanges: Boolean) :
            Mode()


        /**
         * This mode is set after an atom has been replaced.
         * When this mode is active, if the mouse then moves more than a SIG_DELTA, the mode is restored and changed to an atom insertion action
         * @param srcAtom the original atom
         */
        class PostReplacement(val srcAtom: ChemAtom) : Mode()
    }


    companion object {
        private const val SIG_DELTA = 7.5f
    }
}