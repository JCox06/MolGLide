package uk.co.jcox.chemvis.cvengine


import org.joml.Math
import org.joml.Vector2f
import org.joml.Vector3f
import org.joml.Vector4f

import kotlin.math.PI

object Shaper2D {

    private const val W_COMP: Float = 1.0f

    //World space obviously
    fun rectangle(startX: Float, startY: Float, endX: Float, endY: Float) : Mesh {
        val positions = listOf(
            Vector4f(startX + endX, startY + endY, -1.0f, W_COMP), //Top right
            Vector4f(startX + endX, startY - endY, -1.0f, W_COMP), //Bottom Right
            Vector4f(startX - endX, startY - endY, -1.0f, W_COMP), //Bottom Left
            Vector4f(startX - endX, startY + endY, -1.0f, W_COMP) //Top Left
        )

        val textCoord = listOf(
            Vector2f(1.0f, 1.0f),
            Vector2f(1.0f, 0.0f),
            Vector2f(0.0f, 0.0f),
            Vector2f(0.0f, 1.0f),
        )

        val indices = listOf(
            0, 1, 3,
            1, 2, 3
        )

        return Mesh(positions, textCoord, indices)
    }


    fun rectangle(startX: Float, startY: Float, endX: Float, endY: Float,
                  texTR: Vector2f, texBR: Vector2f, texBL: Vector2f, texTL: Vector2f, dx2: Float, dy2: Float) : Mesh {

        val positions = listOf(
            Vector4f(startX + endX - dx2, startY + endY - dy2, -1.0f, W_COMP), //Top right
            Vector4f(startX + endX - dx2, startY - dy2, -1.0f, W_COMP), //Bottom Right
            Vector4f(startX - dx2, startY - dy2, -1.0f, W_COMP), //Bottom Left
            Vector4f(startX -dx2, startY + endY - dy2, -1.0f, W_COMP) //Top Left
        )

        val textCoord = listOf(
            Vector2f(texTR.x, texTR.y),
            Vector2f(texBR.x, texBR.y),
            Vector2f(texBL.x, texBL.y),
            Vector2f(texTL.x, texTL.y),
        )

        val indices = listOf(
            0, 1, 3,
            1, 2, 3
        )

        return Mesh(positions, textCoord, indices)
    }

    fun circle(centreX: Float, centreY: Float, radius: Float, sample: Int = 360) : Mesh {

        val positions: MutableList<Vector4f> = mutableListOf(Vector4f(centreX, centreY, -1.0f, W_COMP))
        val texCoords: MutableList<Vector2f> = mutableListOf(Vector2f(centreX, centreY))

        val indices: MutableList<Int> = mutableListOf(0)

        for (i in 0..sample) {
            val currentAngleRadians = Math.toRadians(i.toFloat())
            val vertX = centreX + radius * Math.cos(currentAngleRadians)
            val vertY = centreY + radius * Math.sin(currentAngleRadians)
            val vertZ = -1.0f
            positions.add(Vector4f(vertX, vertY, vertZ, W_COMP))
            texCoords.add(Vector2f(vertX, vertY))
            indices.add(i + 1)
        }

        return Mesh(positions, texCoords, indices)
    }


    fun line(startX: Float, startY: Float, startZ: Float, endX: Float, endY: Float, endZ: Float) : Mesh {
        val vec1 = Vector4f(startX, startY, startZ, W_COMP)
        val vec2 = Vector4f(endX, endY, endZ, W_COMP)
        val indices: MutableList<Int> = mutableListOf(0, 1)

        val texCoords: MutableList<Vector2f> = mutableListOf(Vector2f(0.0f, 0.0f), Vector2f(0.0f, 0.0f))

        return Mesh(listOf(vec1, vec2), texCoords, indices)
    }
}