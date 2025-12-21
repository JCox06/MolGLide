package uk.co.jcox.chemvis.application.graph

import org.joml.Vector2f
import uk.co.jcox.chemvis.application.MolGLide
import uk.co.jcox.chemvis.application.moleditorstate.BondOrder
import uk.co.jcox.chemvis.application.moleditorstate.StereoChem
import uk.co.jcox.chemvis.cvengine.*


//Todo - While this class works it needs an urgent re-write

class LevelRenderer(
    batcher: Batch2D,
    instancer: InstancedRenderer,
    private val themeStyleManager: ThemeStyleManager,
    private val resources: IResourceManager,
) {

    private val bondRenderer = BondRenderer(instancer, batcher, themeStyleManager)
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
        renderBonds(normalBondsFound, viewport, camera2D, container, StereoChem.IN_PLANE)
        renderBonds(wedgedBondsFound, viewport, camera2D, container, StereoChem.FACING_VIEW)
        renderBonds(dashedBondsFound, viewport, camera2D, container, StereoChem.FACING_PAPER)

        //Make the normal bonds have smooth edges
        bondRenderer.renderBondCircles(normalBondsFound, themeStyleManager.lineThickness * LINE_FACTOR, program, camera2D)
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

    private fun renderBonds(bonds: List<ChemBond>, viewport: Vector2f, camera2D: Camera2D, container: LevelContainer, stereoChem: StereoChem) {
        val program = resources.useProgram(MolGLide.SHADER_LINE)
        val vertexArray = resources.getMesh(CVEngine.MESH_HOLDER_LINE)

        if (stereoChem == StereoChem.IN_PLANE) {
            program.uniform("uWidthMod", 0)
            program.uniform("uDashed", 0)
        }
        if (stereoChem == StereoChem.FACING_VIEW) {
            program.uniform("uWidthMod", 1)
            program.uniform("uDashed", 0)
        }
        if (stereoChem == StereoChem.FACING_PAPER) {
            program.uniform("uWidthMod", 1)
            program.uniform("uDashed", 1)
        }

        program.uniform("camWidthFactor", (viewport.x / camera2D.camWidth ) * LINE_FACTOR)
        bondRenderer.renderBonds(bonds, viewport, camera2D, program, vertexArray, container, stereoChem == StereoChem.IN_PLANE)
    }


    companion object {
        const val LINE_FACTOR = 0.4f
    }
}