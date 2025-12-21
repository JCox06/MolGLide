package uk.co.jcox.chemvis.application.graph

import org.joml.Matrix4f
import org.joml.Vector2f
import org.joml.Vector3f
import org.joml.minus
import org.joml.plus
import org.joml.times
import org.lwjgl.opengl.GL11
import uk.co.jcox.chemvis.application.moleditorstate.BondOrder
import uk.co.jcox.chemvis.application.moleditorstate.OrganicEditorState
import uk.co.jcox.chemvis.cvengine.Batch2D
import uk.co.jcox.chemvis.cvengine.Camera2D
import uk.co.jcox.chemvis.cvengine.GLMesh
import uk.co.jcox.chemvis.cvengine.InstancedRenderer
import uk.co.jcox.chemvis.cvengine.ShaderProgram
import uk.co.jcox.chemvis.cvengine.Shaper2D

class BondRenderer (
    private val instancedRenderer: InstancedRenderer,
    private val batcher: Batch2D,
    private val themeStyleManager: ThemeStyleManager
) {

    fun renderBonds(bonds: List<ChemBond>, viewport: Vector2f, camera2D: Camera2D, bondProgram: ShaderProgram, bondVertexArray: GLMesh, container: LevelContainer, allowExtraLines: Boolean) {
        applyUniforms(bondProgram, camera2D.combined(), viewport, themeStyleManager.lineColour)

        val renderData = mutableListOf<Float>()
        bonds.forEach { bond ->
            prepareRenderData(bonds, renderData, themeStyleManager.lineThickness, container, allowExtraLines)
        }
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        instancedRenderer.drawLines(bondVertexArray, renderData)
        GL11.glDisable(GL11.GL_BLEND)
    }

    private fun applyUniforms(bondProgram: ShaderProgram, camera: Matrix4f, viewport: Vector2f, lineColour: Vector3f) {
        bondProgram.uniform("uPerspective", camera)
        bondProgram.uniform("u_viewport", viewport)
        bondProgram.uniform("uModel", Matrix4f())
        bondProgram.uniform("uLight", lineColour)
    }

    private fun prepareRenderData(bonds: List<ChemBond>, renderData: MutableList<Float>, thickness: Float, container: LevelContainer, allowExtraLines: Boolean) {
        bonds.forEach { bond ->

            var nudge = Vector3f()
            if (bond.centredBond) {
                nudge = bond.bisectorNudge * 2.0f
                //First render a single bond - No Offset Required
                addBond(bond, renderData, -1.0f, thickness, nudge)
            } else {
                //First render a single bond - No Offset Required
                addBond(bond, renderData, 0.0f, thickness, nudge)
            }

            val bondOrder = container.chemManager.getBondOrder(bond.molManagerLink)
            if (! allowExtraLines) {
                return
            }

            if (bondOrder == BondOrder.DOUBLE || bondOrder == BondOrder.TRIPLE) {
                addBond(bond, renderData, 1.0f, thickness, nudge)
            }
            if (bondOrder == BondOrder.TRIPLE) {
                addBond(bond, renderData, -1.0f, thickness, nudge)
            }
        }
    }

    private fun addBond(bond: ChemBond, renderData: MutableList<Float>, offsetFactor: Float, thickness: Float, bisectorNudge: Vector3f = Vector3f()) {
        val flip = if (bond.flipDoubleBond) -1.0f else 1.0f

        val bondStart = bond.atomA.getWorldPosition()
        val bondEnd = bond.atomB.getWorldPosition()
        val diff = bondStart - bondEnd
        val orth = Vector3f(diff.y, -diff.x, diff.z).normalize() * OrganicEditorState.MULTI_BOND_DISTANCE * offsetFactor * flip

        val start = bondStart + orth + bisectorNudge
        val end = bondEnd + orth + bisectorNudge
        renderData.addAll(listOf(start.x, start.y, start.z, end.x, end.y, end.z, thickness))
    }
    
    fun renderBondCircles(bonds: List<ChemBond>, size: Float, shapeProgram: ShaderProgram, camera2D: Camera2D) {

        shapeProgram.uniform("uPerspective", camera2D.combined())
        shapeProgram.uniform("uIgnoreTextures", 1)
        shapeProgram.uniform("uLight", themeStyleManager.lineColour)
        shapeProgram.uniform("uModel", Matrix4f())

        batcher.begin(Batch2D.Mode.FAN)
        for (bond in bonds) {
            val atomAPos = bond.atomA.getWorldPosition()
            val atomBPos = bond.atomB.getWorldPosition()

            val meshA = Shaper2D.circle(atomAPos.x, atomAPos.y, size)
            val meshB = Shaper2D.circle(atomBPos.x, atomBPos.y, size)

            batcher.addBatch(meshA.pack(), meshA.indices)
            batcher.addBatch(meshB.pack(), meshB.indices)
        }
        batcher.end()

        shapeProgram.uniform("uIgnoreTextures", 0)

    }
}