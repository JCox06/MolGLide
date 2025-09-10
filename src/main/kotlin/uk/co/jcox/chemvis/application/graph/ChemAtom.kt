package uk.co.jcox.chemvis.application.graph

import org.joml.Vector3f
import org.joml.plus
import java.util.UUID

class ChemAtom (
    localPos: Vector3f,
    linker: UUID,
    val parent: ChemMolecule
) : ChemObject(localPos, linker) {

    var visible = true
    var implicitHydrogenCount: Int = 0
    var implicitHydrogenPos: RelationalPos = RelationalPos.RIGHT

    fun getWorldPosition() : Vector3f {
        val worldPos = localPos + parent.localPos
        return worldPos
    }

    enum class RelationalPos(val mod: Vector3f) {
        ABOVE(Vector3f(0.0f, 1.0f, 0.0f)),
        LEFT(Vector3f(-1.0f, 0.0f, 0.0f)),
        RIGHT(Vector3f(1.0f, 0.0f, 0.0f)),
        BOTTOM(Vector3f(0.0f, -1.0f, 0.0f)),
    }
}