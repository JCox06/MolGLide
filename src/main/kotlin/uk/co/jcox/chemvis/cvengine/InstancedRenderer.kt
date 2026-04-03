package uk.co.jcox.chemvis.cvengine

import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL15
import org.lwjgl.opengl.GL30
import org.lwjgl.opengl.GL33
import java.lang.AutoCloseable

class InstancedRenderer(private val metrics: CVMetrics) : AutoCloseable {

    private var sharedVertexBuffer: Int = 0


    fun setup() {
        sharedVertexBuffer = GL15.glGenBuffers()
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, sharedVertexBuffer)
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, (SHARED_INSTANCE_SIZE  * Float.SIZE_BYTES).toLong(), GL15.GL_DYNAMIC_DRAW)
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0)
    }


    fun drawMeshes(mesh: GLMesh, instancedData: MutableList<Float>) {
        GL30.glBindVertexArray(mesh.vertexArray)
        bindToVertexArray()


        //This ensures that the shared vertex buffer can takes as much data as possible from the instanced data to limit draw calls
        //Its important to note that the maximum size of the vertex buffer has to "fake" limited to a factor of the mesh mappings removing the remainder
        val factor: Int = SHARED_INSTANCE_SIZE / mesh.openGLMappings
        var loopRun = 0
        val packed = getMaxPackedData(mesh, factor)
        while (true) {
            if (instancedData.size - loopRun * packed > packed) {
                val splice = instancedData.slice(loopRun * packed..<packed * (1 + loopRun))
                drawInstancedMesh(sharedVertexBuffer, splice.toFloatArray(), mesh.vertices, packed / mesh.openGLMappings)
                metrics.completeDraw()
                loopRun++
            } else {
                val data = instancedData.slice(loopRun * packed..<instancedData.size)

                //If the count parameter is wrong, it looks like the level view and the struct view get out of the sync
                //Because I can only assume data is left on the GPU and it just renders the data there
                drawInstancedMesh(sharedVertexBuffer, data.toFloatArray(), mesh.vertices, data.size / mesh.openGLMappings)
                metrics.completeDraw()
                break
            }
        }
    }

    private fun drawInstancedMesh(bufferObject: Int, splicedData: FloatArray, vertices: Int, count: Int) {
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, bufferObject)
        GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, 0L, splicedData)
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0)
        GL33.glDrawElementsInstanced(GL11.GL_POINTS, vertices, GL11.GL_UNSIGNED_INT, 0,count)
    }

    private fun getMaxPackedData(mesh: GLMesh, limitedSize: Int) : Int {
        return mesh.openGLMappings * limitedSize
    }

    fun bindToVertexArray() {
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, sharedVertexBuffer)
    }

    override fun close() {
        GL15.glDeleteBuffers(sharedVertexBuffer)
    }

    companion object {
        private const val SHARED_INSTANCE_SIZE = 5000
    }
}