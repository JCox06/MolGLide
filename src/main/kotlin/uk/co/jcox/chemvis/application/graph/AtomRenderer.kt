package uk.co.jcox.chemvis.application.graph

import org.joml.*
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL30
import uk.co.jcox.chemvis.application.MolGLide
import uk.co.jcox.chemvis.application.moleditorstate.OrganicEditorState
import uk.co.jcox.chemvis.cvengine.*

class AtomRenderer (
    private val batchRenderer: Batch2D,
    private val themeStyleManager: ThemeStyleManager,
    private val resourceManager: IResourceManager,
) {

    fun renderAtoms(levelContainer: LevelContainer, atoms: List<ChemAtom>, camera2D: Camera2D, textProgram: ShaderProgram) {
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)

        //First check to see if the atom is visible, if it's not, do not render!
        for (atom in atoms) {
            if (! atom.visible) {
                continue
            }
            val atomSymbol = levelContainer.chemManager.getAtomInsert(atom.molManagerLink).symbol
            val length = drawFormulaString(atomSymbol, atom.getWorldPosition(), textProgram, camera2D)

            drawAssociatedAtomGroup(atom, levelContainer, length, textProgram, camera2D)
        }

        GL11.glDisable(GL11.GL_BLEND)
    }

    private fun drawFormulaString(label: String, pos: Vector3f, textProgram: ShaderProgram, camera2D: Camera2D) : Float {
        val fontID = MolGLide.FONT
        val scale = MolGLide.GLOBAL_SCALE
        val colour = getSymbolColour(label)
        val fontData = resourceManager.getFont(fontID)
        resourceManager.useTexture(fontID, GL30.GL_TEXTURE0)
        textProgram.uniform("uTexture0", 0)
        textProgram.uniform("uLight", colour)
        textProgram.uniform("uModel", Matrix4f())
        textProgram.uniform("uPerspective", camera2D.combined())
        val width = drawCharacters(label, pos, fontData, scale)
        return width
    }

    /**
     * @return the length of the label - The total glyph advance width
     */
    private fun drawCharacters(label: String, pos: Vector3f, font: BitmapFont, scale: Float) : Float {
        var renderX = pos.x
        var renderY = pos.y

        val char1 = label.first()
        val metrics = font.glyphs[char1] ?: return 0.0f
        val dx = scale * metrics.glyphWidth
        val dy = scale * metrics.glyphHeight
        renderX -= dx / 2
        renderY -= dy / 2

        batchRenderer.begin(Batch2D.Mode.TRIANGLES)

        for (c in label) {
            var character = c
            if (! font.glyphs.keys.contains(c)) {
                character = font.glyphs.keys.first()
            }

            val advance = drawCharacter(character, font, scale, renderX, renderY)
            renderX += advance
        }
        batchRenderer.end()

        return renderX - pos.x + dx/2
    }

    /**
     * Draws a character to the batch renderer
     * @param character the character to draw
     * @param font The bitmap font to use
     * @param renderX Where to start rendering
     * @param renderY Where to start rendering
     *
     * @return The glyph width advance
     */
    private fun drawCharacter(character: Char, font: BitmapFont, scale: Float, renderX: Float, renderY: Float) : Float {

        val metrics = font.glyphs[character] ?: return 0.0f

        var scaleMod = 1.0f

        if (character.isDigit()) {
            scaleMod = DIGIT_SCALE
        }

        val width = scaleMod * metrics.glyphWidth * scale
        val height = scaleMod * metrics.glyphHeight * scale

        val meshToDraw = Shaper2D.rectangle(
            renderX, renderY, width, height,
            Vector2f(
                metrics.textureUnitAddX + metrics.textureUnitX,
                metrics.textureUnitAddY - metrics.textureUnitY
            ),
            Vector2f(metrics.textureUnitAddX + metrics.textureUnitX, 0.0f - metrics.textureUnitY),
            Vector2f(0.0f + metrics.textureUnitX, 0.0f - metrics.textureUnitY),
            Vector2f(0.0f + metrics.textureUnitX, metrics.textureUnitAddY - metrics.textureUnitY)
        )

        batchRenderer.addBatch(meshToDraw.pack(), meshToDraw.indices)

        return width
    }

    private fun calculateFormulaStringWidth(label: String, font: BitmapFont, scale: Float) : Float {
        var size = 0.0f
        for (character in label) {
            var actualGlyph = character
            if (! font.glyphs.keys.contains(character)) {
                actualGlyph = font.glyphs.keys.first()
            }

            var scaleMod = 1.0f
            if (character.isDigit()) {
                scaleMod = DIGIT_SCALE
            }

            val metrics = font.glyphs[actualGlyph] ?: return 0.0f
            val width = metrics.glyphWidth * scale * scaleMod
            size += width
        }
        return size
    }


    /**
     * @return total label width
     */
    private fun drawAssociatedAtomGroup(chemAtom: ChemAtom, levelContainer: LevelContainer, widthTransform: Float, textProgram: ShaderProgram, camera2D: Camera2D) : Float {
        //Right now this will only draw implicit hydrogens - But once custom insertion through typing is implemented, this will handle that as well!
        val protons = levelContainer.chemManager.getImplicitHydrogens(chemAtom.molManagerLink)
        if (protons == 0) {
            return 0.0f
        }
        val posMod = chemAtom.implicitHydrogenPos

        val string = if (protons == 1) "H" else "H${protons}"

        val newPos = when (posMod) {
            ChemAtom.RelationalPos.ABOVE -> {chemAtom.getWorldPosition() + posMod.mod * OrganicEditorState.IMPLICIT_SCALE}
            ChemAtom.RelationalPos.LEFT -> {chemAtom.getWorldPosition() + Vector3f(-calculateFormulaStringWidth(string, resourceManager.getFont(
                MolGLide.FONT), MolGLide.GLOBAL_SCALE), 0.0f, 0.0f)
            }
            ChemAtom.RelationalPos.RIGHT -> {chemAtom.getWorldPosition() + Vector3f(widthTransform, 0.0f, 0.0f) }
            ChemAtom.RelationalPos.BOTTOM -> {chemAtom.getWorldPosition() + posMod.mod * OrganicEditorState.IMPLICIT_SCALE }
        }

        val width = drawFormulaString(string, newPos, textProgram, camera2D)

        return width
    }


    private fun getSymbolColour(symbol: String): Vector3f {
        val colour = themeStyleManager.symbolColours[symbol] ?: return themeStyleManager.lineColour
        return colour
    }

    companion object {
        const val DIGIT_SCALE = 0.6f
    }
}