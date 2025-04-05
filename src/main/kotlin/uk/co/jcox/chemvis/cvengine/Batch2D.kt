package uk.co.jcox.chemvis.cvengine

import org.lwjgl.opengl.*
import kotlin.math.max

class Batch2D (
    private val batchSize: Long = DEFAULT_BATCH_SIZE,
) : AutoCloseable {
    private val elementSize: Long = DEFAULT_BATCH_SIZE * 3L


    private val vertices: MutableList<Float> = mutableListOf()
    private val indices: MutableList<Int> = mutableListOf()
    private val glVertexArray: Int = GL30.glGenVertexArrays()
    private val glVertexBuffer: Int = GL15.glGenBuffers()
    private val glIndexBuffer: Int = GL15.glGenBuffers()

    private val textureUnitArray = IntArray(GL11.glGetInteger(GL30.GL_MAX_TEXTURE_UNITS))

    init {
        //Setup up OpenGL Objects
        GL30.glBindVertexArray(this.glVertexArray)

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, this.glVertexBuffer)
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, batchSize, GL15.GL_DYNAMIC_DRAW)

        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, this.glIndexBuffer)
        GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, elementSize, GL15.GL_DYNAMIC_DRAW)

        //Map OpenGL attribute objects
        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, VERT_SIZE, 0)
        GL20.glVertexAttribPointer(1, 2, GL11.GL_FLOAT, false, VERT_SIZE, 3L * Float.SIZE_BYTES)
        GL20.glVertexAttribPointer(2, 1, GL11.GL_FLOAT, false, VERT_SIZE, 5L * Float.SIZE_BYTES)
        GL20.glEnableVertexAttribArray(0)
        GL20.glEnableVertexAttribArray(1)
        GL20.glEnableVertexAttribArray(2)


        //Map texture units
        for (i in textureUnitArray.indices) {
            textureUnitArray[i] = i
        }
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

        //todo stop vertex size extending its limit


        //Step 2 - Map batch indices to offsetindices
        val mappedIndices = batchIndices.map {
            it + (max(0, this.vertexCount() - 2))
        }

        //Step 1 - Push everything from this vertex to list into the master list
        this.vertices.addAll(batchVertices)

        this.indices.addAll(mappedIndices)
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

    fun mapProgramTextures(program: ShaderProgram) {
        program.uniform("textures", textureUnitArray)
    }

    private fun vertexCount(): Int {
        return this.indices.size
    }

    companion object {
        //Vertex
        //[3 float pos] [2 float texture] [1 texID] = 6 floats
        private const val VERT_SIZE = 6 * Float.SIZE_BYTES
        private const val DEFAULT_BATCH_SIZE = VERT_SIZE * 500L
    }

    override fun close() {
        GL30.glBindVertexArray(0)
        GL15.glDeleteBuffers(this.glVertexBuffer)
        GL15.glDeleteBuffers(this.glIndexBuffer)
        GL30.glDeleteVertexArrays(this.glVertexArray)
    }
}