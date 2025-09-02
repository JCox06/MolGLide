package uk.co.jcox.chemvis.application.graph

import org.joml.Vector3f
import java.util.UUID

class ChemAtom (
    localPos: Vector3f,
    linker: UUID,
    val text: String,
) : ChemObject(localPos, linker) {
}