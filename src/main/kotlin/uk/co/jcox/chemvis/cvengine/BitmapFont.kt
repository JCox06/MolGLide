package uk.co.jcox.chemvis.cvengine

import org.joml.Vector2f
import org.joml.Vector3f
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL15

class BitmapFont (
    val fontSize: Int,
    private val textureManager: TextureManager,
    private val textureID: String,
    private val glyphs: Map<Char, GlyphData>,

    ) {



    fun text(label: String, batcher: Batch2D, program: ShaderProgram, xpos: Float, ypos: Float) {
        text(label, Vector3f(1.0f, 1.0f, 1.0f), batcher, program, xpos, ypos, 1.0f)
    }

    //WARNING: Batcher must be in a rest state before this method is called!
    fun text(label: String, colour: Vector3f, batcher: Batch2D, program: ShaderProgram, xpos: Float, ypos: Float, scale: Float) {

        val scaledFont = fontSize * scale

        program.bind()
        program.uniform("mainTexture", 0)
        program.uniform("fontColour", colour)
        textureManager.useTexture(textureID, GL15.GL_TEXTURE0)
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        batcher.begin(GL11.GL_TRIANGLES)

        var renderX = xpos
        var renderY = ypos

        for (c in label.toCharArray()) {
            var toDraw = c
            if (! glyphs.keys.contains(c)) {
                toDraw = glyphs.keys.first()
            }

            //Now start drawing the shapes
            val glyphData = glyphs[toDraw]
            if (glyphData == null) {
                batcher.end()
                return
            }

            val mesh = Shaper2D.rectangle(renderX, renderY, glyphData.glyphWidth * scale, glyphData.glyphHeight * scale,
                Vector2f(glyphData.textureUnitAddX + glyphData.textureUnitX, glyphData.textureUnitAddY - glyphData.textureUnitY),
                Vector2f(glyphData.textureUnitAddX + glyphData.textureUnitX, 0.0f - glyphData.textureUnitY),
                Vector2f(0.0f + glyphData.textureUnitX, 0.0f - glyphData.textureUnitY),
                Vector2f(0.0f + glyphData.textureUnitX, glyphData.textureUnitAddY - glyphData.textureUnitY)
            )

//            val mesh = Shaper2D.rectangle(renderX , renderY, size.toFloat(), size.toFloat())

            batcher.addBatch(mesh)
            renderX += glyphData.glyphWidth * scale
        }
        batcher.end()
        program.uniform("fontColour", Vector3f(1.0f, 1.0f, 1.0f))
        GL11.glDisable(GL11.GL_BLEND);
    }



    data class GlyphData (
        val glyphWidth: Float,
        val glyphHeight: Float,
        val textureUnitX: Float,
        val textureUnitY: Float,
        val textureUnitAddX: Float,
        val textureUnitAddY: Float,
    )
}