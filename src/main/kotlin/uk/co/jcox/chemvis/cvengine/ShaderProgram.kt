package uk.co.jcox.chemvis.cvengine

import org.joml.Matrix4f
import org.joml.Vector2f
import org.joml.Vector3f
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL20
import org.lwjgl.opengl.GL30
import org.tinylog.Logger

class ShaderProgram (
    private val program: Int
) : AutoCloseable {


    fun bind() {
        GL30.glUseProgram(this.program)
    }


    fun uniform(name: String, value: Float) {
        GL30.glUniform1f(getUniformLocation(name), value)
    }

    fun uniform(name: String, value: Int) {
        GL30.glUniform1i(getUniformLocation(name), value)
    }

    fun uniform(name: String, value: Vector3f) {
        GL30.glUniform3f(getUniformLocation(name), value.x, value.y, value.z)
    }

    fun uniform(name: String, value: Vector2f) {
        GL30.glUniform2f(getUniformLocation(name), value.x, value.y)
    }

    fun uniform(name: String, value: IntArray) {
        GL30.glUniform1iv(getUniformLocation(name), value)
    }

    fun uniform(name: String, value: Matrix4f) {
        val location = getUniformLocation(name)
        val buff = BufferUtils.createFloatBuffer(16)
        value[buff]
        GL20.glUniformMatrix4fv(location, false, buff)
    }

    private fun getUniformLocation(name: String): Int {
        val location = GL30.glGetUniformLocation(this.program, name)
        if (location == -1) {
//            throw RuntimeException("Error - No uniform location found with ${name} at loc ${location}")
            Logger.error { "No uniform location found with $name" }
        }
        bind()
        return location
    }

    override fun close() {
        GL30.glDeleteProgram(this.program)
    }
}