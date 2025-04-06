package uk.co.jcox.chemvis.cvengine

object Shaper2D {

    //World space obviously
    fun rectangle(startX: Float, startY: Float, endX: Float, endY: Float) : Mesh {
        val vertices = listOf(
            startX + endX, startY + endY, -1.0f, 1.0f, 1.0f,
            startX + endX, startY, -1.0f, 1.0f, 0.0f,
            startX, startY, -1.0f, 0.0f, 0.0f,
            startX, startY + endY, -1.0f, 0.0f, 1.0f,
        )

        val indices = listOf(
            0, 1, 3,
            1, 2, 3
        )

        return Mesh(vertices, indices)
    }
}