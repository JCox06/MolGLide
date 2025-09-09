package uk.co.jcox.chemvis.cvengine

import org.joml.Vector2f
import org.joml.Vector3f
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL15

class BitmapFont (
    val fontSize: Int,
    private val fontID: String,
    val glyphs: Map<Char, GlyphData>,

    ) {

    data class GlyphData (
        val glyphWidth: Float,
        val glyphHeight: Float,
        val textureUnitX: Float,
        val textureUnitY: Float,
        val textureUnitAddX: Float,
        val textureUnitAddY: Float,
    )
}