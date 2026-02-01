package uk.co.jcox.chemvis.application.graph

import org.joml.Matrix4f
import org.joml.Vector2f
import org.joml.Vector3f
import org.joml.plus
import org.joml.times
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL30
import org.xmlcml.euclid.Vector3
import uk.co.jcox.chemvis.application.MolGLide
import uk.co.jcox.chemvis.application.moleditorstate.OrganicEditorState
import uk.co.jcox.chemvis.cvengine.*


//Todo - While this class works it needs an urgent re-write

class LevelRenderer(
    private val batcher: Batch2D,
    private val instancer: InstancedRenderer,
    private val themeStyleManager: ThemeStyleManager,
    private val resources: IResourceManager,
) {

    private val lineData = mutableListOf<Float>()



    fun renderLevel(commonRenderer: CommonRenderer, camera2D: Camera2D, viewport: Vector2f) {

        renderLines(commonRenderer, camera2D, viewport)
        renderElements(commonRenderer, camera2D, viewport)
    }

    private fun renderLines(commonRenderer: CommonRenderer, camera2D: Camera2D, viewport: Vector2f) {
        val program = resources.useProgram(MolGLide.SHADER_LINE)
        val vertexArray = resources.getMesh(CVEngine.MESH_HOLDER_LINE)
        program.uniform("uPerspective", camera2D.combined())
        program.uniform("u_viewport", viewport)
        program.uniform("uModel", Matrix4f())
        program.uniform("uLight", themeStyleManager.lineColour)
        program.uniform("camWidthFactor", (viewport.x / camera2D.camWidth) * LINE_FACTOR)

        //Get the normal lines
        program.uniform("uWidthMod", 0)
        program.uniform("uDashed", 0)
        commonRenderer.getLineData().forEach { line ->
            packLineData(line, lineData)
        }
        instancer.drawLines(vertexArray, lineData)

        lineData.clear()
        
        //Get the wedged lines
        program.uniform("uWidthMod", 1)
        commonRenderer.getWedgeData().forEach { line ->
            packLineData(line, lineData)
        }
        instancer.drawLines(vertexArray, lineData)
        
        lineData.clear()
        
        program.uniform("uDashed", 1)
        commonRenderer.getDashedData().forEach { line ->
            packLineData(line, lineData)
        }
        instancer.drawLines(vertexArray, lineData)

        lineData.clear()

        val shapeProgram = resources.useProgram(CVEngine.SHADER_SIMPLE_TEXTURE)

        renderBondCircles(commonRenderer.getLineData(), themeStyleManager.lineThickness * LINE_FACTOR, shapeProgram, camera2D )
    }

    private fun packLineData(line: CommonRenderer.LineElement, packedData: MutableList<Float>) {
        packedData += (line.start.x)
        packedData += (line.start.y)
        packedData += (line.start.z)
        packedData += (line.end.x)
        packedData += (line.end.y)
        packedData += (line.end.z)
        packedData += (themeStyleManager.lineThickness)
    }


    private fun renderElements(commonRenderer: CommonRenderer, camera2D: Camera2D, viewport: Vector2f) {
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)


        val textProgram = resources.useProgram(CVEngine.SHADER_SIMPLE_TEXTURE)
        commonRenderer.getElements().forEach { atom ->
            val atomSymbol = atom.text
            val length = drawFormulaString(atomSymbol, atom.position, textProgram, camera2D)
            atom.group?.let { group ->
                drawAssociatedAtomGroup(group.text, group.relationalPos, atom.position, length, textProgram, camera2D)
            }
        }
    }


    private fun drawFormulaString(label: String, pos: Vector3f, textProgram: ShaderProgram, camera2D: Camera2D) : Float {
        val fontID = MolGLide.FONT
        val scale = MolGLide.GLOBAL_SCALE
        val fontData = resources.getFont(fontID)
        resources.useTexture(fontID, GL30.GL_TEXTURE0)
        textProgram.uniform("uTexture0", 0)
        textProgram.uniform("uLight", Vector3f(1.0f, 1.0f, 1.0f))
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

        batcher.begin(Batch2D.Mode.TRIANGLES)

        for (c in label) {
            var character = c
            if (! font.glyphs.keys.contains(c)) {
                character = font.glyphs.keys.first()
            }

            val advance = drawCharacter(character, font, scale, renderX, renderY)
            renderX += advance
        }
        batcher.end()

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
            scaleMod = CommonRenderer.DIGIT_SCALE
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

        batcher.addBatch(meshToDraw.pack(), meshToDraw.indices)

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
                scaleMod = CommonRenderer.DIGIT_SCALE
            }

            val metrics = font.glyphs[actualGlyph] ?: return 0.0f
            val width = metrics.glyphWidth * scale * scaleMod
            size += width
        }
        return size
    }


    private fun drawAssociatedAtomGroup(formulaString: String, relationalPos: ChemAtom.RelationalPos, absPos: Vector3f, widthTransform: Float, textProgram: ShaderProgram, camera2D: Camera2D) : Float {

        val newPos = when (relationalPos) {
            ChemAtom.RelationalPos.ABOVE -> {absPos + relationalPos.mod * OrganicEditorState.IMPLICIT_SCALE}
            ChemAtom.RelationalPos.LEFT -> {absPos + Vector3f(-calculateFormulaStringWidth(formulaString, resources.getFont(
                MolGLide.FONT), MolGLide.GLOBAL_SCALE), 0.0f, 0.0f)
            }
            ChemAtom.RelationalPos.RIGHT -> {absPos + Vector3f(widthTransform, 0.0f, 0.0f) }
            ChemAtom.RelationalPos.BOTTOM -> {absPos + relationalPos.mod * OrganicEditorState.IMPLICIT_SCALE }
        }

        val width = drawFormulaString(formulaString, newPos, textProgram, camera2D)

        return width
    }

    private fun renderBondCircles(bonds: List<CommonRenderer.LineElement>, size: Float, shapeProgram: ShaderProgram, camera2D: Camera2D) {

        shapeProgram.uniform("uPerspective", camera2D.combined())
        shapeProgram.uniform("uIgnoreTextures", 1)
        shapeProgram.uniform("uLight", themeStyleManager.lineColour)
        shapeProgram.uniform("uModel", Matrix4f())

        //todo instance this - THis is the cause of performance problems = Too many draw calls
        batcher.begin(Batch2D.Mode.FAN)
        for (bond in bonds) {
            val atomAPos = bond.start
            val atomBPos = bond.end

            val meshA = Shaper2D.circle(atomAPos.x, atomAPos.y, size)
            val meshB = Shaper2D.circle(atomBPos.x, atomBPos.y, size)

            batcher.addBatch(meshA.pack(), meshA.indices)
            batcher.addBatch(meshB.pack(), meshB.indices)
        }
        batcher.end()

        shapeProgram.uniform("uIgnoreTextures", 0)

    }


    companion object {
        const val LINE_FACTOR = 0.4f
    }
}