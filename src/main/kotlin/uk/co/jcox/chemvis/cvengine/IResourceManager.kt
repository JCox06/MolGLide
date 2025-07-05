package uk.co.jcox.chemvis.cvengine

import org.lwjgl.opengl.GL15
import java.io.File

interface IResourceManager {

    /**
     * Load a vertex shader, (optionally) a geometry shader, and a fragment shader into a program
     * @param id the string id of the shader
     * @param vertSrc the file to the vertex shader source
     * @param fragSrc the file to the fragment shader source
     * @param geomSrc the file to the geometry shader (use null for no geometry shader)
     */
    fun loadShadersFromDisc(id: String, vertSrc: File, fragSrc: File, geomSrc: File? = null)


    /**
     * Bind a program with a specific ID
     * @param programID the id of the program to bind
     * @return The shader program
     */
    fun useProgram(programID: String): ShaderProgram

    fun destroyProgram(programID: String)


    /**
     * Manage a CPU side Mesh object into a GLMesh object internally
     * The instanced Renderer will use its Instanced VBO to add the vertex attributes to the VAO
     * @param id specify the id of the mesh to manage
     * @param mesh the cpu side mesh to manage
     * @param instancedRenderer An instance renderer - Usually the Engine's Instance Renderer
     */
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


    /**
     * Load a true type font into a texture image to draw text
     * @param id the font id to use
     * @param font the file to the true type font
     * @param glyphs a string of the letters you want in the final texture
     * @param size the font size to use
     */
    fun loadFontFromDisc(id: String, font: File, glyphs: String, size: Int)

    fun getFont(id: String) : BitmapFont

    fun useTexture(id: String, textureUnit: Int): Boolean

    /**
     * Render targets allow you to render to a different frame buffer/texture rather than the main one
     * This method allows you to create a custom render target with a custom ID
     * By default the main window has a frame buffer id of null
     * @param id The ID you wish to use for this render target
     */
    fun createMultiSampledRenderTarget(id: String, samples: Int = 4)

    fun destroyRenderTarget(id: String)

    fun getRenderTarget(id: String) : RenderTarget

    fun resizeRenderTarget(id: String, proposedWidth: Float, proposedHeight: Float)
}