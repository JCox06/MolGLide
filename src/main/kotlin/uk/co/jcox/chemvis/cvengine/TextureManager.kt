package uk.co.jcox.chemvis.cvengine

import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL15
import org.lwjgl.opengl.GL30

class TextureManager (
    private val maxTextureUnits: Int = GL11.glGetInteger(GL30.GL_MAX_TEXTURE_UNITS)
) :AutoCloseable {

    private val managedTextures: MutableMap<String, Int> = mutableMapOf()


    fun manageTexture(id: String, glTexture: Int) {
        this.managedTextures[id] = glTexture
    }

    fun useTexture(id: String, textureUnit: Int): Boolean {
        GL15.glActiveTexture(textureUnit)
        val textureToBind = this.managedTextures[id]
        if (textureToBind != null) {
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureToBind)
            return true
        }
        return false
    }

    fun deleteTexture(id: String) {
        val textureObject = this.managedTextures[id]
        if (textureObject != null) {
            GL11.glDeleteTextures(textureObject)
        }

        this.managedTextures.remove(id)
    }


    override fun close() {
        managedTextures.values.forEach() {
            GL11.glDeleteTextures(it)
        }

        this.managedTextures.clear()
    }

}