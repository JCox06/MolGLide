package uk.co.jcox.chemvis.cvengine

import org.lwjgl.opengl.GL15
import java.io.File

interface IResourceManager {

    fun loadShadersFromDisc(id: String, vertSrc: File, fragSrc: File, geomSrc: File? = null)
    fun useProgram(programID: String): ShaderProgram
    fun destroyProgram(programID: String)

    fun manageMesh(id: String, mesh: Mesh, instancedRenderer: InstancedRenderer)

    fun manageMaterial(id: String, material: Material)

    fun destroyMesh(id: String)

    fun destroyMaterial(id: String)

    fun getMaterial(id: String) : Material

    fun getMesh(id: String) : GLMesh

    fun loadTextureFromDisc(id: String, texture: File)

    fun destroyTexture(id: String)

    fun destroyFont(id: String)

    fun destroy()

    fun loadFontFromDisc(id: String, font: File, glyphs: String, size: Int)

    fun getFont(id: String) : BitmapFont

    fun useTexture(id: String, textureUnit: Int): Boolean

    fun createRenderTarget(id: String)

    fun destroyRenderTarget(id: String)

    fun getRenderTarget(id: String) : RenderTarget

    fun resizeRenderTarget(id: String, proposedWidth: Float, proposedHeight: Float)
}