package uk.co.jcox.chemvis.application

import org.joml.Vector2f
import org.joml.Vector3f
import org.lwjgl.opengl.GL11
import uk.co.jcox.chemvis.application.chemengine.IMoleculeManager
import uk.co.jcox.chemvis.cvengine.Batch2D
import uk.co.jcox.chemvis.cvengine.BitmapFont
import uk.co.jcox.chemvis.cvengine.ShaderProgram
import uk.co.jcox.chemvis.cvengine.Shaper2D

class ChemLevelRenderer {

    fun renderBondFormationPreview(atomA: Vector2f, atomB: Vector2f, batcher: Batch2D) {
        batcher.begin(GL11.GL_LINES)
        batcher.addBatch(listOf(atomA.x, atomA.y, 0.0f, atomB.x, atomB.y, 0.0f), listOf(0, 1))
        batcher.end()
    }

    fun renderSelectedAtom(selectedPosition: Vector2f, size: Float, program: ShaderProgram, batcher: Batch2D) {
        program.uniform("textureMode", 0)
        program.uniform("fontColour", Vector3f(0.1f, 0.1f, 0.1f))
        batcher.begin(GL11.GL_TRIANGLE_FAN)
        batcher.addBatch(Shaper2D.circle(selectedPosition.x, selectedPosition.y, size))
        batcher.end()
        program.uniform("textureMode", 1)
    }

    fun renderLevel(level: ChemLevel, molManager: IMoleculeManager, batcher: Batch2D, font: BitmapFont, program: ShaderProgram) {
        //Render atoms
        font.blend = true
        molManager.molecules().forEach { mol ->
            molManager.relatedAtoms(mol).forEach { atom ->
                val pos = level.getPosition(atom)
                font.text(molManager.getAtomSymbol(atom), batcher, program, pos.x - (font.glyphSize() / 2), pos.y - (font.glyphSize() / 2))
            }
        }
    }
}