package uk.co.jcox.chemvis.cvengine

import org.joml.Matrix4f
import org.joml.Vector2f
import org.joml.Vector3f
import org.joml.Vector4f
import org.joml.times

data class Mesh(
    val positions: List<Vector4f>,
    val texCoordinates: List<Vector2f>,
    val indices: List<Int>
) {
    
    fun pack() : List<Float> {

        val meshData: MutableList<Float> = mutableListOf()
        
        for (i in 0..< positions.size) {
            val pos = positions[i]
            meshData.add(pos.x)
            meshData.add(pos.y)
            meshData.add(pos.z)
            val tex = texCoordinates[i]
            meshData.add(tex.x)
            meshData.add(tex.y)
        }

        return meshData
    }

    fun apply(transform: Matrix4f) : Mesh {
        val newPos = positions.map { it -> it.times(transform) }
        return Mesh(newPos, texCoordinates, indices)
    }
}
