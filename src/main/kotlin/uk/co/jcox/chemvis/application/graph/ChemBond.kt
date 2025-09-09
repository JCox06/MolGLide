package uk.co.jcox.chemvis.application.graph

import org.joml.Vector3f
import java.util.UUID

class ChemBond(
    val atomA: ChemAtom,
    val atomB: ChemAtom,
    var type: Type, localPos: Vector3f, molManagerLink: UUID,

    ) : ChemObject(localPos, molManagerLink) {

    enum class Type {
        SINGLE,
        DOUBLE,
    }
}