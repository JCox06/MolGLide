package uk.co.jcox.chemvis.application.moleditorstate.action

import uk.co.jcox.chemvis.application.graph.ChemMolecule
import uk.co.jcox.chemvis.application.graph.LevelContainer

class DirectMoleculeCreationAction (
    private val x: Float,
    private val y: Float,
    private val z: Float,
    
    
) : IAction {

    lateinit var newMolecule: ChemMolecule

    override fun execute(levelContainer: LevelContainer) {
        val mol = ChemMolecule()
        mol.positionOffset.x = x
        mol.positionOffset.y = y
        mol.positionOffset.z = z

        levelContainer.sceneMolecules.add(mol)

        newMolecule = mol
    }

    override fun undo(levelContainer: LevelContainer) {
        newMolecule?.let { levelContainer.sceneMolecules.remove(it) }
    }
}