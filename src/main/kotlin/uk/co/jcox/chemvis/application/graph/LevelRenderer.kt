package uk.co.jcox.chemvis.application.graph

import org.joml.Matrix4f
import org.joml.Vector2f
import org.joml.Vector3f
import org.joml.minus
import org.joml.plus
import org.joml.times
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL30
import uk.co.jcox.chemvis.application.MolGLide
import uk.co.jcox.chemvis.application.chemengine.BondOrder
import uk.co.jcox.chemvis.application.chemengine.IMoleculeManager
import uk.co.jcox.chemvis.application.moleditorstate.OrganicEditorState
import uk.co.jcox.chemvis.cvengine.Batch2D
import uk.co.jcox.chemvis.cvengine.CVEngine
import uk.co.jcox.chemvis.cvengine.Camera2D
import uk.co.jcox.chemvis.cvengine.IResourceManager
import uk.co.jcox.chemvis.cvengine.InstancedRenderer
import uk.co.jcox.chemvis.cvengine.ShaderProgram
import uk.co.jcox.chemvis.cvengine.Shaper2D

class LevelRenderer(
    private val batcher: Batch2D,
    private val instancer: InstancedRenderer,
    private val resources: IResourceManager,
    private val themeStyleManager: ThemeStyleManager
) {


    /**
     * As with the old system, rendering a level hasn't changed.
     * 1) Group entities by their rendering type (Text, Other Mesh, etc)
     * 2) Render them with the instanced Renderer or the batch renderer
     * @param container the level to render
     * @param camera2D the position of the camera in the scene
     * @param viewport the viewport of the current window - used for some shaders
     * */
    fun renderLevel(container: LevelContainer, camera2D: Camera2D, viewport: Vector2f) {

        //Group everything together
        val atomEntities: MutableList<ChemAtom> = mutableListOf()
        val bondEntities: MutableList<ChemBond> = mutableListOf()

        traverseAndCollect(container, atomEntities, bondEntities)

        //Now Render
        renderAtomSymbols(container, atomEntities, camera2D)

        renderBondLines(container, bondEntities, camera2D, viewport)
    }


    private fun traverseAndCollect(
        container: LevelContainer,
        atomsFound: MutableList<ChemAtom>,
        bondsFound: MutableList<ChemBond>
    ) {

        //Go through every molecule, noting down the molecule position
        container.sceneMolecules.forEach { mol ->

            //Now go through every atom
            mol.atoms.forEach { atom ->
                atomsFound.add(atom)
            }

            mol.bonds.forEach { bond ->
                bondsFound.add(bond)
            }
        }
    }


    private fun renderBondLines(container: LevelContainer, bonds: List<ChemBond>, camera2D: Camera2D, viewport: Vector2f) {
        val lineProgram = resources.useProgram(CVEngine.SHADER_INSTANCED_LINE)
        lineProgram.uniform("uPerspective", camera2D.combined())
        lineProgram.uniform("u_viewport", viewport)
        lineProgram.uniform("uModel", Matrix4f())

        val glMesh = resources.getMesh(CVEngine.MESH_HOLDER_LINE)

        val instanceData = mutableListOf<Float>()

        val lineColour = themeStyleManager.activeTheme.lineColour

        lineProgram.uniform("uLight", lineColour)

        for (line in bonds) {
            prepareLineRenderData(container.chemManager, line, instanceData)
        }
        instancer.drawLines(glMesh, instanceData)
    }


    private fun prepareLineRenderData(chemManager: IMoleculeManager, line: ChemBond, renderData: MutableList<Float>) {
        val lineTypeOrder = chemManager.getBondOrder(line.molManagerLink)
        if (lineTypeOrder == BondOrder.SINGLE) {
            renderSingleBond(chemManager, line, renderData)
        }
        if (lineTypeOrder == BondOrder.DOUBLE) {
            renderDoubleBond(chemManager, line, renderData)
        }
    }

    private fun renderSingleBond(chemManager: IMoleculeManager, line: ChemBond, renderData: MutableList<Float>) {
        val start = line.atomA.getWorldPosition()
        val end = line.atomB.getWorldPosition()
        renderData.addAll(listOf(start.x, start.y, start.z, end.x, end.y, end.z, themeStyleManager.activeTheme.lineThickness))
    }

    private fun renderDoubleBond(chemManager: IMoleculeManager, line: ChemBond, renderData: MutableList<Float>) {
        val start = line.atomA.getWorldPosition()
        val end = line.atomB.getWorldPosition()


        val diff = start - end
        val orth = Vector3f(diff.y, -diff.x, diff.z).normalize() * OrganicEditorState.MULTI_BOND_DISTANCE

        if (line.flipDoubleBond) {
            orth.negate()
        }

        if (line.centredBond) {
            val newOffset = orth.div(-2.0f, Vector3f())
            newOffset.add(line.bisectorNudge)
            start.add(newOffset)
            end.add(newOffset)
        }

        val newStart = start + orth
        val newEnd = end + orth

        renderData.addAll(listOf(start.x, start.y, start.z, end.x, end.y, end.z, themeStyleManager.activeTheme.lineThickness))

        val secondData = listOf<Float>(newStart.x, newStart.y, newStart.z, newEnd.x, newEnd.y, newEnd.z, themeStyleManager.activeTheme.lineThickness)
        renderData.addAll(secondData)

    }


    private fun renderAtomSymbols(leveLContainer: LevelContainer, atoms: List<ChemAtom>, camera2D: Camera2D) {
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        val textProgram = resources.useProgram(CVEngine.SHADER_SIMPLE_TEXTURE)
        textProgram.uniform("uPerspective", camera2D.combined())

        atoms.forEach { atom ->
            //Render the atom symbol
            val worldPos = atom.getWorldPosition()
            if (atom.visible) {
                renderString(leveLContainer.chemManager.getAtomInsert(atom.molManagerLink).symbol, worldPos, textProgram)
            }

            val hydrogen = leveLContainer.chemManager.getImplicitHydrogens(atom.molManagerLink)

            //Check to see if this atom has any implicit hydrogens
            if (hydrogen >= 1 && atom.visible) {

                var numberString = "$hydrogen"

                if (hydrogen == 1) {
                    numberString = ""
                }

                if (atom.implicitHydrogenPos != ChemAtom.RelationalPos.LEFT) {


                    val implicitHPos = atom.implicitHydrogenPos.mod * OrganicEditorState.IMPLICIT_SCALE

                    if(atom.implicitHydrogenPos == ChemAtom.RelationalPos.ABOVE || atom.implicitHydrogenPos == ChemAtom.RelationalPos.BOTTOM) {
                        implicitHPos.mul(4/3f)
                    }

                    renderString("H${numberString}", implicitHPos + worldPos, textProgram)

                } else {
                    val implicitHPos = atom.implicitHydrogenPos.mod * OrganicEditorState.IMPLICIT_SCALE * (numberString.length.toFloat() + 1.0f)
                    renderString("H${numberString}", implicitHPos + worldPos, textProgram)
                }

            }
        }

        GL11.glDisable(GL11.GL_BLEND)
    }

    //todo rewrite but it works for now
    private fun renderString(label: String, pos: Vector3f, program: ShaderProgram) {
        val theme = themeStyleManager.activeTheme

        val fontID = MolGLide.FONT
        val scale = MolGLide.GLOBAL_SCALE
        val colour = getSymbolColour(label)

        val fontData = resources.getFont(fontID)


        resources.useTexture(fontID, GL30.GL_TEXTURE0)
        program.uniform("uTexture0", 0)
        program.uniform("uLight", colour)
        program.uniform("uModel", Matrix4f())


        var renderX = pos.x
        var renderY = pos.y

        batcher.begin(Batch2D.Mode.TRIANGLES)


        for (c in label) {
            var character = c

            if (!fontData.glyphs.keys.contains(c)) {
                character = fontData.glyphs.keys.first()
            }

            val glyphMetrics = fontData.glyphs[character]

            if (glyphMetrics == null) {
                return
            }

            var scaleMod = 1.0f

            if (c.isDigit()) {
                scaleMod = 0.75f
                renderX -= glyphMetrics.glyphWidth * scale * scaleMod / 4
                renderY -= glyphMetrics.glyphHeight * scale * scaleMod / 4
            }

            val width = scaleMod * glyphMetrics.glyphWidth * scale / 2
            val height = scaleMod * glyphMetrics.glyphHeight * scale / 2

            //Does not work!
//            val meshToDraw = Shaper2D.rectangle(renderX + width, renderY + height, width, height, (Text rendering works fine If you use this line instead of the one below) - However then the text is uncentred
            val meshToDraw = Shaper2D.rectangle(
                renderX, renderY, width, height,
                Vector2f (
                    glyphMetrics.textureUnitAddX + glyphMetrics.textureUnitX,
                    glyphMetrics.textureUnitAddY - glyphMetrics.textureUnitY
                ),
                Vector2f(glyphMetrics.textureUnitAddX + glyphMetrics.textureUnitX, 0.0f - glyphMetrics.textureUnitY),
                Vector2f(0.0f + glyphMetrics.textureUnitX, 0.0f - glyphMetrics.textureUnitY),
                Vector2f(0.0f + glyphMetrics.textureUnitX, glyphMetrics.textureUnitAddY - glyphMetrics.textureUnitY)
            )

            batcher.addBatch(meshToDraw.pack(), meshToDraw.indices)
            renderX += width * 2
        }

        batcher.end()
    }


    private fun getSymbolColour(symbol: String): Vector3f {
        val colour = themeStyleManager.activeTheme.symbolColours[symbol] ?: return themeStyleManager.activeTheme.lineColour
        return colour
    }
}