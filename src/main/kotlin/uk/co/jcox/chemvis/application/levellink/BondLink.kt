package uk.co.jcox.chemvis.application.levellink

import org.joml.Vector2f
import org.joml.Vector4f
import java.util.UUID

data class BondLink(
    val bondID: UUID,
    val pos: Vector4f
)
