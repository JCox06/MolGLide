package uk.co.jcox.chemvis.application.graph

import org.joml.Vector2f
import org.joml.Vector3f
import org.joml.plus
import org.openscience.cdk.Atom
import org.openscience.cdk.interfaces.IAtom
import uk.co.jcox.chemvis.application.moleditorstate.AtomInsert
import java.io.Serializable
import java.util.UUID
import javax.vecmath.Point2d

class ChemAtom (
    insert: AtomInsert,
    val iAtom: IAtom = Atom(insert.symbol)
) {

    var visible = true
    var implicitHydrogenPos: RelationalPos = RelationalPos.RIGHT
    lateinit var parent: ChemMolecule


    /**
     * This is to allow methyl groups to be optionally labeled as Me
     * For other groups such as Et, Pr, I don' think this method is required
     * I can't seem to find a way to do this directly on the CDK so this is one example where the Depiction Generator will not respect the choice of the Me group
     */
    var priorityName: String? = null


    fun getWorldPosition() : Vector3f {
        val atomX = iAtom.point2d.x.toFloat()
        val atomY = iAtom.point2d.y.toFloat()
        val parentPos = parent.positionOffset

        val worldPos = parentPos + Vector3f(atomX, atomY, 0.0f)
        return worldPos
    }

    fun getSymbol() : String {
        return iAtom.symbol
    }

    fun setInnerPosition(x: Float, y: Float) {
        iAtom.point2d = Point2d(x.toDouble(), y.toDouble())
    }

    enum class RelationalPos(val mod: Vector3f) : Serializable {
        ABOVE(Vector3f(0.0f, 1.0f, 0.0f)),
        LEFT(Vector3f(-1.0f, 0.0f, 0.0f)),
        RIGHT(Vector3f(1.0f, 0.0f, 0.0f)),
        BOTTOM(Vector3f(0.0f, -1.0f, 0.0f)),

    }
}