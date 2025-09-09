package uk.co.jcox.chemvis.application.graph

import org.joml.Vector3f
import org.joml.Vector4f

data class ThemeStyle(
    val backgroundColour: Vector4f,

    val defaultAtomColour: Vector3f,

    val symbolColours: Map<String, Vector3f>,


    val lineColour: Vector3f,

)
