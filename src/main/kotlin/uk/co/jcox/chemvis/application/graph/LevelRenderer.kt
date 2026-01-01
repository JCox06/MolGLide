package uk.co.jcox.chemvis.application.graph

import org.joml.Vector2f
import org.openscience.cdk.interfaces.IBond
import org.openscience.cdk.isomorphism.TransformOp
import uk.co.jcox.chemvis.application.MolGLide
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
        renderBonds(normalBondsFound, viewport, camera2D, container, IBond.Display.Solid)
        renderBonds(wedgedBondsFound, viewport, camera2D, container, IBond.Display.WedgeEnd)
        renderBonds(dashedBondsFound, viewport, camera2D, container, IBond.Display.WedgedHashEnd)

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
                val order = bond.iBond.order
                if (order == IBond.Order.DOUBLE || order == IBond.Order.TRIPLE) {
                    normalBondsFound.add(bond)
                    return@forEach
                }
                val stereochem = bond.getStereo()
                when (stereochem) {
                    IBond.Display.Solid -> normalBondsFound.add(bond)
                    IBond.Display.WedgeEnd -> wedgedBondsFound.add(bond)
                    IBond.Display.WedgedHashEnd -> dashedBondsFound.add(bond)
                    else -> normalBondsFound.add(bond)
                }
            }
        }
    }

    private fun renderBonds(bonds: List<ChemBond>, viewport: Vector2f, camera2D: Camera2D, container: LevelContainer, stereoChem: IBond.Display) {
        val program = resources.useProgram(MolGLide.SHADER_LINE)
        val vertexArray = resources.getMesh(CVEngine.MESH_HOLDER_LINE)

        if (stereoChem == IBond.Display.Solid) {
            program.uniform("uWidthMod", 0)
            program.uniform("uDashed", 0)
        }
        if (stereoChem == IBond.Display.WedgeEnd) {
            program.uniform("uWidthMod", 1)
            program.uniform("uDashed", 0)
        }
        if (stereoChem == IBond.Display.WedgedHashEnd) {
            program.uniform("uWidthMod", 1)
            program.uniform("uDashed", 1)
        }

        program.uniform("camWidthFactor", (viewport.x / camera2D.camWidth ) * LINE_FACTOR)
        bondRenderer.renderBonds(bonds, viewport, camera2D, program, vertexArray, container, stereoChem == IBond.Display.Solid)
    }


    companion object {
        const val LINE_FACTOR = 0.4f
    }
}