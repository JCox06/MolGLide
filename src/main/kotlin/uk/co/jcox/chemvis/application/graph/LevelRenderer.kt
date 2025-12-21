package uk.co.jcox.chemvis.application.graph

import org.joml.Vector2f
import uk.co.jcox.chemvis.application.MolGLide
import uk.co.jcox.chemvis.application.moleditorstate.BondOrder
import uk.co.jcox.chemvis.application.moleditorstate.StereoChem
import uk.co.jcox.chemvis.cvengine.*


//Todo - While this class works it needs an urgent re-write

class LevelRenderer(
    private val batcher: Batch2D,
    private val instancer: InstancedRenderer,
    private val resources: IResourceManager,
    private val themeStyleManager: ThemeStyleManager
) {

    private val bondRenderer = BondRenderer(instancer, themeStyleManager)
    private val atomRenderer = AtomRenderer(batcher, themeStyleManager, resources)

    fun renderLevel(container: LevelContainer, camera2D: Camera2D, viewport: Vector2f) {
        //Group everything together
        val atomEntities: MutableList<ChemAtom> = mutableListOf()
        val normalBondsFound: MutableList<ChemBond> = mutableListOf()
        val wedgedBondsFound: MutableList<ChemBond> = mutableListOf()
        val dashedBondsFound: MutableList<ChemBond> = mutableListOf()
        traverseAndCollect(container, atomEntities, normalBondsFound, wedgedBondsFound, dashedBondsFound)


        //Render the atoms
        val program = resources.useProgram(CVEngine.SHADER_SIMPLE_TEXTURE)
        atomRenderer.renderAtoms(container, atomEntities, camera2D, program)

        //Render the bonds
        renderNormalLines(normalBondsFound, viewport, camera2D, container)
        renderWedgedLines(wedgedBondsFound, viewport, camera2D, container)
        renderDashedLines(dashedBondsFound, viewport, camera2D, container)
    }

    private fun traverseAndCollect(
        container: LevelContainer,
        atomsFound: MutableList<ChemAtom>,
        normalBondsFound: MutableList<ChemBond>,
        wedgedBondsFound: MutableList<ChemBond>,
        dashedBondsFound: MutableList<ChemBond>,
    ) {
        //Go through every molecule, noting down the molecule position
        container.sceneMolecules.forEach { mol ->
            //Now go through every atom
            mol.atoms.forEach { atom ->
                atomsFound.add(atom)
            }
            mol.bonds.forEach { bond ->
                val order = container.chemManager.getBondOrder(bond.molManagerLink)
                if (order == BondOrder.DOUBLE || order == BondOrder.TRIPLE) {
                    normalBondsFound.add(bond)
                    return@forEach
                }
                val stereochem = container.chemManager.getStereoChem(bond.molManagerLink)
                when (stereochem) {
                    StereoChem.IN_PLANE -> normalBondsFound.add(bond)
                    StereoChem.FACING_VIEW -> wedgedBondsFound.add(bond)
                    StereoChem.FACING_PAPER -> dashedBondsFound.add(bond)
                }
            }
        }
    }

    private fun renderNormalLines(bonds: List<ChemBond>, viewport: Vector2f, camera2D: Camera2D, container: LevelContainer) {
        val program = resources.useProgram(CVEngine.SHADER_INSTANCED_LINE)
        val vertexArray = resources.getMesh(CVEngine.MESH_HOLDER_LINE)
        bondRenderer.renderBonds(bonds, viewport, camera2D, program, vertexArray, container, true)
    }

    private fun renderWedgedLines(bonds: List<ChemBond>, viewport: Vector2f, camera2D: Camera2D, container: LevelContainer) {
        val program = resources.useProgram(MolGLide.SHADER_WEDGED_LINE)
        val vertexArray = resources.getMesh(CVEngine.MESH_HOLDER_LINE)
        bondRenderer.renderBonds(bonds, viewport, camera2D, program, vertexArray, container, false)
    }

    private fun renderDashedLines(bonds: List<ChemBond>, viewport: Vector2f, camera2D: Camera2D, container: LevelContainer) {
        val program = resources.useProgram(MolGLide.SHADER_DASHED_LINE)
        val vertexArray = resources.getMesh(CVEngine.MESH_HOLDER_LINE)
        bondRenderer.renderBonds(bonds, viewport, camera2D, program, vertexArray, container, false)
    }
}