package uk.co.jcox.chemvis.application.graph

import org.joml.Vector3f
import org.joml.Vector4f

data class ThemeStyle(
    val backgroundColour: Vector4f,

    val hydrogenColour: Vector3f,
    val carbonColour: Vector3f,
    val nitrogenColour: Vector3f,
    val oxygenColour: Vector3f,
    val sulphurColour: Vector3f,
    val phosphorusColour: Vector3f,
    val fluorineColour: Vector3f,
    val chlorineColour: Vector3f,
    val bromineColour: Vector3f,
    val metalColour: Vector3f,
    val defaultAtomColour: Vector3f,

    val lineColour: Vector3f,

)
