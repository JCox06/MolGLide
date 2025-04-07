package uk.co.jcox.chemvis.cvengine

import org.lwjgl.opengl.*
import kotlin.math.max

class Batch2D (

    //Vertex
    //[3 float pos] [2 float texture] = 5 floats
    private val vertexCapacity: Int = 200
) : AutoCloseable {

    private val batchSizeBytes = vertexCapacity * VERTEX_SIZE_BYTES
    private val elementSizeBytes = vertexCapacity * 3 * Int.SIZE_BYTES

    private val vertices: MutableList<Float> = mutableListOf()
    private val indices: MutableList<Int> = mutableListOf()
    private val glVertexArray: Int = GL30.glGenVertexArrays()
    private val glVertexBuffer: Int = GL15.glGenBuffers()
    private val glIndexBuffer: Int = GL15.glGenBuffers()

    init {
        //Setup up OpenGL Objects
        GL30.glBindVertexArray(this.glVertexArray)

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, this.glVertexBuffer)
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, batchSizeBytes.toLong(), GL15.GL_DYNAMIC_DRAW)

        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, this.glIndexBuffer)
        GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, elementSizeBytes.toLong(), GL15.GL_DYNAMIC_DRAW)

        //Map OpenGL attribute objects
        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, VERTEX_SIZE_BYTES, 0)
        GL20.glVertexAttribPointer(1, 2, GL11.GL_FLOAT, false, VERTEX_SIZE_BYTES, 3L * Float.SIZE_BYTES)
        GL20.glEnableVertexAttribArray(0)
        GL20.glEnableVertexAttribArray(1)

    }

    private var ready = false
    private var mode = 0

    fun begin(mode: Int) {
        if (this.ready) {
            throw RuntimeException("Begin called twice - Batcher was already ready")
        }

        this.mode = mode
        GL30.glBindVertexArray(this.glVertexArray)
        this.ready = true
    }


    fun addBatch(batchVertices: List<Float>, batchIndices: List<Int>) {
        if (! this.ready) {
            throw RuntimeException("Batcher not ready for batching")
        }


        if (vertexCount() + (batchVertices.size / VERTEX_SIZE) >= vertexCapacity) {
            println("Batcher has reached maxed capacity, drawing now -> then restarting")
            val modeRestore = mode
            end()
            begin(modeRestore)
        }


        //Step 2 - Map batch indices to offsetindices
        val mappedIndices = batchIndices.map {
            it + (max(0, this.vertexCount()))
        }

        //Step 1 - Push everything from this vertex to list into the master list
        this.vertices.addAll(batchVertices)

        this.indices.addAll(mappedIndices)
    }


    fun addBatch(mesh: Mesh) {
        this.addBatch(mesh.vertices, mesh.indices)
    }


    fun end() {
        if (!this.ready) {
            throw RuntimeException("End called twice - Batcher has already finished")
        }

        //Send all the data to the GPU
        val vertexBuff = this.vertices.toFloatArray()
        GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, 0, vertexBuff)

        val elementBuff = this.indices.toIntArray()
        GL15.glBufferSubData(GL15.GL_ELEMENT_ARRAY_BUFFER, 0, elementBuff)



        GL15.glDrawElements(mode, this.indices.size, GL11.GL_UNSIGNED_INT, 0)


        //Clear cpu buffers
        this.vertices.clear()
        this.indices.clear()


        this.ready = false
    }


    private fun vertexCount(): Int {
        return this.vertices.size / VERTEX_SIZE
    }


    override fun close() {
        GL30.glBindVertexArray(0)
        GL15.glDeleteBuffers(this.glVertexBuffer)
        GL15.glDeleteBuffers(this.glIndexBuffer)
        GL30.glDeleteVertexArrays(this.glVertexArray)
    }

    companion object {
        //[3 float pos] [2 float texture] = 5 floats
        private val VERTEX_SIZE = 5
        private val VERTEX_SIZE_BYTES = 5 * Float.SIZE_BYTES
    }
}