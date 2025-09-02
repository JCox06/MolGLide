package uk.co.jcox.chemvis.application.graph

import org.joml.Vector3f
import java.util.UUID

class ChemMolecule (
    localPos: Vector3f,
    bridge: UUID,
) : ChemObject(localPos, bridge) {

    val atoms = mutableListOf<ChemAtom>()
    val bonds = mutableListOf<ChemBond>()
}