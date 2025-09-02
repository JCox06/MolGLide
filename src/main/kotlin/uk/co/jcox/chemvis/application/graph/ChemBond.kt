package uk.co.jcox.chemvis.application.graph

import org.joml.Vector3f

class ChemBond(
    val atomA: ChemAtom,
    val atomB: ChemAtom,

    val offset: Vector3f,
    val type: Type,
) {

    enum class Type {
        SINGLE,
        DOUBLE,
    }
}