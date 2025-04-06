package uk.co.jcox.chemvis.cvengine

import org.freehep.graphicsio.font.truetype.TTFGlyfTable.Glyph
import org.joml.Vector2f
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL15

class BitmapFont (
    val size: Int,
    private val textureManager: TextureManager,
    private val textureID: String,
    private val glyphs: Map<Char, GlyphData>,

    ) {


    //WARNING: Batcher must be in a rest state before this method is called!
    fun text(label: String, batcher: Batch2D, program: ShaderProgram, xpos: Float, ypos: Float) {
        program.bind()
        program.uniform("mainTexture", 0)
        textureManager.useTexture(textureID, GL15.GL_TEXTURE0)

        batcher.begin(GL11.GL_TRIANGLES)

        var renderX = xpos
        var renderY = ypos

        for (c in label.toCharArray()) {
            var toDraw = c
            if (! glyphs.keys.contains(c)) {
                toDraw = glyphs.keys.first()
            }

            //Now start drawing the shapes
            val glyphData = glyphs[c]
            if (glyphData == null) {
                batcher.end()
                return
            }

            val mesh = Shaper2D.rectangle(renderX, renderY, size.toFloat(), size.toFloat(),
                Vector2f(glyphData.textureUnitAddX + glyphData.textureUnitX, glyphData.textureUnitAddY - glyphData.textureUnitY),
                Vector2f(glyphData.textureUnitAddX + glyphData.textureUnitX, 0.0f - glyphData.textureUnitY),
                Vector2f(0.0f + glyphData.textureUnitX, 0.0f - glyphData.textureUnitY),
                Vector2f(0.0f + glyphData.textureUnitX, glyphData.textureUnitAddY - glyphData.textureUnitY)
            )

//            val mesh = Shaper2D.rectangle(renderX , renderY, size.toFloat(), size.toFloat())

            batcher.addBatch(mesh)
            renderX += size
        }

        batcher.end()
    }

    data class GlyphData (
        val textureUnitX: Float,
        val textureUnitY: Float,
        val textureUnitAddX: Float,
        val textureUnitAddY: Float,
    )
}