package uk.co.jcox.chemvis.application.graph

import org.joml.Vector3f
import org.joml.plus
import java.util.UUID

class ChemAtom (
    localPos: Vector3f,
    linker: UUID,
    val text: String,
    val parent: ChemMolecule
) : ChemObject(localPos, linker) {


    fun getWorldPosition() : Vector3f {
        val worldPos = localPos + parent.localPos
        return worldPos
    }
}