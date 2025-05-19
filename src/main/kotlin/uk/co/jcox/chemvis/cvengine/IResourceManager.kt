package uk.co.jcox.chemvis.cvengine

import org.lwjgl.opengl.GL15
import java.io.File

interface IResourceManager {

    fun loadShadersFromDisc(id: String, vertSrc: File, fragSrc: File)
    fun useProgram(programID: String): ShaderProgram
    fun destroyProgram(programID: String)

    fun manageMesh(id: String, mesh: Mesh)

    fun destroyMesh(id: String)

    fun loadTextureFromDisc(id: String, texture: File)

    fun destroyTexture(id: String)

    fun destroyFont(id: String)

    fun destroy()

    fun loadFontFromDisc(id: String, font: File, glyphs: String, size: Int)

    fun getFont(id: String) : BitmapFont

    fun useTexture(id: String, textureUnit: Int): Boolean
}