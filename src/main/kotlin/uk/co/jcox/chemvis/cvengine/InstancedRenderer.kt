package uk.co.jcox.chemvis.cvengine

import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL15
import org.lwjgl.opengl.GL30
import org.lwjgl.opengl.GL33
import java.lang.AutoCloseable

class InstancedRenderer : AutoCloseable {

    var lineInstancedBuffer: Int = 0
    private set


    fun setup() {
        lineInstancedBuffer = GL15.glGenBuffers()
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, lineInstancedBuffer)
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, (3 * INSTANCES * Float.SIZE_BYTES).toLong(), GL15.GL_DYNAMIC_DRAW)
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0)
    }


    fun drawLines(mesh: GLMesh, instancedData: List<Float>, count: Int) {
        GL30.glBindVertexArray(mesh.vertexArray)
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, lineInstancedBuffer)
        GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, 0L, instancedData.toFloatArray())
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0)
        GL33.glDrawElementsInstanced(GL11.GL_POINTS, mesh.vertices, GL11.GL_UNSIGNED_INT, 0,count)
    }

    override fun close() {
        GL15.glDeleteBuffers(lineInstancedBuffer)
    }

    companion object {
        private const val INSTANCES = 100
    }
}