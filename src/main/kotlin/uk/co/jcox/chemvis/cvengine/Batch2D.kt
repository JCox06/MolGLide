package uk.co.jcox.chemvis.cvengine

import org.lwjgl.opengl.*
import uk.co.jcox.chemvis.cvengine.CVEngine.Companion.VERTEX_SIZE
import uk.co.jcox.chemvis.cvengine.CVEngine.Companion.VERTEX_SIZE_BYTES
import kotlin.math.max

class Batch2D (

    //Vertex
    //[3 float pos] [2 float texture] = 5 floats = (Standard Vertex Set)
    //todo there is an error if during one #addToBatch call it overloads the whole buffer everthing breaks
    //so for the meantime the capacity has been increased
    private val vertexCapacity: Int = 500
) : AutoCloseable {

    private val batchSizeBytes = vertexCapacity * VERTEX_SIZE_BYTES
    private val elementSizeBytes = vertexCapacity * 3 * Int.SIZE_BYTES

    private val vertices: MutableList<Float> = mutableListOf()
    private val indices: MutableList<Int> = mutableListOf()
    private var glVertexArray: Int =0
    private var glVertexBuffer: Int = 0
    private var glIndexBuffer: Int = 0

    fun setup() {

        glVertexArray = GL30.glGenVertexArrays()
        glVertexBuffer = GL15.glGenBuffers()
        glIndexBuffer = GL15.glGenBuffers()

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

        GL30.glBindVertexArray(0)
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0)
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0)
    }

    private var ready = false
    private var mode = Mode.TRIANGLES

    fun begin(mode: Mode) {
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
            val modeRestore = mode
            end()
            begin(modeRestore)
        }


        //Step 2 - Map batch indices to offsetindices
        val mappedIndices = batchIndices.map {
            val res = it + (max(0, this.vertexCount()))
            res
        }

        //Step 1 - Push everything from this vertex to list into the master list
        this.vertices.addAll(batchVertices)

        this.indices.addAll(mappedIndices)
    }


    fun end() : Mode {
        if (!this.ready) {
            throw RuntimeException("End called twice - Batcher has already finished")
        }

        GL30.glBindVertexArray(glVertexArray)
        //Apparently the VAO does not store array buffers
        //So if something came along between batcher setup and the next bit of code
        //We will generate a bunch of errors
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, glVertexArray)

        //Send all the data to the GPU
        val vertexBuff = this.vertices.toFloatArray()

        GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, 0, vertexBuff)

        val elementBuff = this.indices.toIntArray()
        GL15.glBufferSubData(GL15.GL_ELEMENT_ARRAY_BUFFER, 0, elementBuff)

        GL15.glDrawElements(mode.openGlID, this.indices.size, GL11.GL_UNSIGNED_INT, 0)



        //Clear cpu buffers
        this.vertices.clear()
        this.indices.clear()


        this.ready = false

        GL30.glBindVertexArray(0)
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0)

        return mode
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

    enum class Mode (val openGlID: Int) {
        TRIANGLES(GL11.GL_TRIANGLES),
        FAN(GL11.GL_TRIANGLE_FAN),
        LINE(GL11.GL_LINES),
    }
}