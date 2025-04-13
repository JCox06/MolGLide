package uk.co.jcox.chemvis.application

import org.joml.Vector2f
import org.joml.Vector4f
import org.openscience.cdk.interfaces.IAtomContainer

data class Molecule(
    val friendlyName: String,
    val editorPosition: Vector4f,
    val cdkLink: IAtomContainer,
)
