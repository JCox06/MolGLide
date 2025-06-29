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
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, (LINE_VERTEX_DATA * LINE_INSTANCES * Float.SIZE_BYTES).toLong(), GL15.GL_DYNAMIC_DRAW)
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0)
    }


    fun drawLines(mesh: GLMesh, instancedData: MutableList<Float>) {
        GL30.glBindVertexArray(mesh.vertexArray)
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, lineInstancedBuffer)

        var loopRun = 0
        while (true) {
            if (instancedData.size - loopRun * PACKED_DATA > PACKED_DATA) {
                val splice = instancedData.slice(loopRun * PACKED_DATA..< PACKED_DATA * (1 + loopRun))
                drawInstancedLine(lineInstancedBuffer, splice.toFloatArray(), mesh.vertices, LINE_INSTANCES)
                loopRun++
            } else {
                val data = instancedData.slice(loopRun * PACKED_DATA..< instancedData.size)

                //If the count parameter is wrong, it looks like the level view and the struct view get out of the sync
                //Because I can only assume data is left on the GPU and it just renders the data there
                drawInstancedLine(lineInstancedBuffer, data.toFloatArray(), mesh.vertices, data.size / LINE_VERTEX_DATA)
                break
            }
        }
    }

    private fun drawInstancedLine(bufferObject: Int, splicedData: FloatArray, vertices: Int, count: Int) {
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, bufferObject)
        GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, 0L, splicedData)
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0)
        GL33.glDrawElementsInstanced(GL11.GL_POINTS, vertices, GL11.GL_UNSIGNED_INT, 0,count)
    }
    override fun close() {
        GL15.glDeleteBuffers(lineInstancedBuffer)
    }

    companion object {
        private const val LINE_INSTANCES = 500
        private const val LINE_VERTEX_DATA = 7
        private const val PACKED_DATA = LINE_INSTANCES * LINE_VERTEX_DATA
    }
}