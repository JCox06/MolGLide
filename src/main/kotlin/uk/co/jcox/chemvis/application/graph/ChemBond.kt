package uk.co.jcox.chemvis.application.graph

import org.joml.Vector3f
import org.joml.plus
import org.openscience.cdk.Bond
import org.openscience.cdk.interfaces.IBond
import org.openscience.cdk.smiles.smarts.parser.SMARTSParserConstants.a
import java.util.UUID

class ChemBond(
    val atomA: ChemAtom,
    val atomB: ChemAtom,
    val iBond: IBond = Bond(atomA.iAtom, atomB.iAtom)
    ) {
    /**
     * This flag is used to hint to the LevelRenderer to centre the bond if it is a double bond
     * It is often used for Carbonyl bonds or Imine bonds
     *
     * Although this could be dynamically calculated during render-time, it is instead set at action time
     * This allows the user to override the parameter
     */
    var centredBond = false

    var bisectorNudge = Vector3f(0.0f, -1.0f, 0.0f)

    var flipDoubleBond = false

    val bondOffset = Vector3f()


    fun getMidpoint() : Vector3f {
        val atomAPos = atomA.getWorldPosition()
        val atomBPos = atomB.getWorldPosition()

        val midpoint = (atomAPos + atomBPos) /2f

        val corrected = midpoint + bondOffset

        return corrected
    }

    fun setStereo(newDisplay: IBond.Display) {
        iBond.display = newDisplay
    }

    fun getStereo() : IBond.Display {
        return iBond.display
    }
}