package uk.co.jcox.chemvis.cvengine

import org.joml.Vector2f

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


    fun rectangle(startX: Float, startY: Float, endX: Float, endY: Float,
                  texTR: Vector2f, texBR: Vector2f, texBL: Vector2f, texTL: Vector2f) : Mesh {
        val vertices = listOf(
            startX + endX, startY + endY, -1.0f, texTR.x, texTR.y,
            startX + endX, startY, -1.0f, texBR.x, texBR.y,
            startX, startY, -1.0f, texBL.x, texBL.y,
            startX, startY + endY, -1.0f, texTL.x, texTL.y
        )

        val indices = listOf(
            0, 1, 3,
            1, 2, 3
        )

        return Mesh(vertices, indices)
    }
}