package uk.co.jcox.chemvis.application.moleditorstate.tool

import com.github.jsonldjava.shaded.com.google.common.base.Verify
import org.joml.Vector3f
import uk.co.jcox.chemvis.application.graph.ChemAtom
import uk.co.jcox.chemvis.application.graph.ChemMolecule
import uk.co.jcox.chemvis.application.graph.LevelContainer
import uk.co.jcox.chemvis.application.moleditorstate.AtomInsert
import uk.co.jcox.chemvis.application.moleditorstate.OrganicEditorState
import uk.co.jcox.chemvis.application.moleditorstate.SelectionManager
import uk.co.jcox.chemvis.cvengine.Camera2D
import uk.co.jcox.chemvis.cvengine.IRenderTargetContext
import uk.co.jcox.chemvis.cvengine.InputManager
import java.util.UUID

class AtomBondTool (
    toolboxContext: ToolboxContext, renderingContext: IRenderTargetContext, inputManager: InputManager, camera2D: Camera2D, levelContainer: LevelContainer, selectionManager: SelectionManager,
) : Tool(toolboxContext, renderingContext, inputManager, camera2D, levelContainer, selectionManager){

    var toolMode: Mode = Mode.None

    override fun onClick(clickX: Float, clickY: Float) {
        toolMode = chooseCorrectMode()

        when (val mode = toolMode) {
            is Mode.None -> {}
            is Mode.MolCreation -> createNewMolecule()
        }
    }

    override fun onRelease(clickX: Float, clickY: Float) {
        if (selectionManager.primarySelection is SelectionManager.Type.None) {
            val newMolecule = ChemMolecule(Vector3f(clickX, clickY, OrganicEditorState.ATOM_PLANE), UUID.randomUUID())
            val newAtom = ChemAtom(Vector3f(0.0f, 0.0f, 0.0f), UUID.randomUUID(), "C", newMolecule)

            newMolecule.atoms.add(newAtom)

            levelContainer.sceneMolecules.add(newMolecule)
        }
    }


    private fun createNewMolecule() {
    }


    private fun chooseCorrectMode() : Mode {
        return Mode.MolCreation
    }

    sealed class Mode {
        object None: Mode()
        object MolCreation: Mode()
    }
}