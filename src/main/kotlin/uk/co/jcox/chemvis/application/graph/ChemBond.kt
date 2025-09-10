package uk.co.jcox.chemvis.application.graph

import org.joml.Vector3f
import java.util.UUID

class ChemBond(
    val atomA: ChemAtom,
    val atomB: ChemAtom,
    localPos: Vector3f, molManagerLink: UUID,

    ) : ChemObject(localPos, molManagerLink) {

    /**
     * This flag is used to hint to the LevelRenderer to centre the bond if it is a double bond
     * It is often used for Carbonyl bonds or Imine bonds
     *
     * Although this could be dynamically calculated during render-time, it is instead set at action time
     * This allows the user to override the parameter
     */
    var centredBond = false

    var bisectorNudge = Vector3f(0.0f, -1.0f, 0.0f)


}