package uk.co.jcox.chemvis.application.graph

import org.joml.Matrix4f
import org.joml.Vector2f
import org.joml.Vector3f
import org.joml.plus
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL30
import uk.co.jcox.chemvis.application.MolGLide
import uk.co.jcox.chemvis.cvengine.Batch2D
import uk.co.jcox.chemvis.cvengine.CVEngine
import uk.co.jcox.chemvis.cvengine.Camera2D
import uk.co.jcox.chemvis.cvengine.IResourceManager
import uk.co.jcox.chemvis.cvengine.InstancedRenderer
import uk.co.jcox.chemvis.cvengine.ShaderProgram
import uk.co.jcox.chemvis.cvengine.Shaper2D

class LevelRenderer (
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

        traverseAndCollect(container, atomEntities)

        //Now Render
        renderAtomSymbols(atomEntities, camera2D)
    }


    private fun traverseAndCollect(container: LevelContainer, atomsFound: MutableList<ChemAtom>) {

        //Go through every molecule, noting down the molecule position
        container.sceneMolecules.forEach { mol ->

            //Now go through every atom
            mol.atoms.forEach { atom ->
                atomsFound.add(atom)
            }
        }
    }


    private fun renderAtomSymbols(atoms: List<ChemAtom>, camera2D: Camera2D) {
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        val textProgram = resources.useProgram(CVEngine.SHADER_SIMPLE_TEXTURE)
        textProgram.uniform("uPerspective", camera2D.combined())

        atoms.forEach { atom, ->
            renderSymbol(atom, textProgram)
        }

        GL11.glDisable(GL11.GL_BLEND)
    }

    private fun renderSymbol(entity: ChemAtom, program: ShaderProgram) {
        val theme = themeStyleManager.activeTheme

        val fontID = MolGLide.FONT
        val scale = MolGLide.GLOBAL_SCALE
        val colour = getSymbolColour(entity.text)

        val fontData = resources.getFont(fontID)


        resources.useTexture(fontID, GL30.GL_TEXTURE0)
        program.uniform("uTexture0", 0)
        program.uniform("uLight", colour)
        program.uniform("uModel", Matrix4f())

        val pos = entity.getWorldPosition()

        var renderX = pos.x
        val renderY = pos.y

        batcher.begin(Batch2D.Mode.TRIANGLES)


        for (c in entity.text) {
            var character = c

            if (! fontData.glyphs.keys.contains(c)) {
                character = fontData.glyphs.keys.first()
            }

            val glyphMetrics = fontData.glyphs[character]

            if (glyphMetrics == null) {
                return
            }

            val width = glyphMetrics.glyphWidth *  scale /2
            val height = glyphMetrics.glyphHeight * scale /2

            //Does not work!
//            val meshToDraw = Shaper2D.rectangle(renderX + width, renderY + height, width, height, (Text rendering works fine If you use this line instead of the one below) - However then the text is uncentred
            val meshToDraw = Shaper2D.rectangle(renderX, renderY, width, height,
                Vector2f(glyphMetrics.textureUnitAddX + glyphMetrics.textureUnitX, glyphMetrics.textureUnitAddY - glyphMetrics.textureUnitY),
                Vector2f(glyphMetrics.textureUnitAddX + glyphMetrics.textureUnitX, 0.0f - glyphMetrics.textureUnitY),
                Vector2f(0.0f + glyphMetrics.textureUnitX, 0.0f - glyphMetrics.textureUnitY),
                Vector2f(0.0f + glyphMetrics.textureUnitX, glyphMetrics.textureUnitAddY - glyphMetrics.textureUnitY)
            )

            batcher.addBatch(meshToDraw.pack(), meshToDraw.indices)
            renderX += width * 2
        }

        batcher.end()
    }


    private fun getSymbolColour(symbol: String) : Vector3f {
        return Vector3f(1.0f, 1.0f, 1.0f)
    }
}