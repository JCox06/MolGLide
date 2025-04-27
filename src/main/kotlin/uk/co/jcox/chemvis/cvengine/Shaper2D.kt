package uk.co.jcox.chemvis.cvengine

import org.apache.commons.lang3.mutable.Mutable
import org.joml.Math
import org.joml.Vector2f
import org.openscience.cdk.smiles.smarts.parser.SMARTSParserConstants.x
import kotlin.math.PI

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

    fun circle(centreX: Float, centreY: Float, radius: Float, sample: Int = 360) : Mesh {
        val vertices: MutableList<Float> = mutableListOf(centreX, centreY, -1.0f, centreX, centreY)
        val indices: MutableList<Int> = mutableListOf(0)

        for (i in 0..sample) {
            val currentAngleRadians = Math.toRadians(i.toFloat())
            val vertX = centreX + radius * Math.cos(currentAngleRadians)
            val vertY = centreY + radius * Math.sin(currentAngleRadians)
            val vertZ = -1.0f
            vertices.add(vertX)
            vertices.add(vertY)
            vertices.add(vertZ)
            vertices.add(vertX)
            vertices.add(vertY)
            indices.add(i + 1)
        }

        return Mesh(vertices, indices)
    }
}