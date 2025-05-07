package uk.co.jcox.chemvis.application.states

import org.joml.Vector3f
import uk.co.jcox.chemvis.cvengine.Batch2D
import uk.co.jcox.chemvis.cvengine.BitmapFont
import uk.co.jcox.chemvis.cvengine.IApplicationState
import uk.co.jcox.chemvis.cvengine.ShaderProgram

class EditorState (
    val batcher: Batch2D,
    val font: BitmapFont,
    val program: ShaderProgram,
) : IApplicationState {

    override fun init() {
        println("The editor state has been created!")
    }

    override fun update() {

    }

    override fun render() {
        font.text("Welcome to the Editor! Enjoy your stay", Vector3f(1.0f, 0.0f, 0.0f), batcher, program, 100.0f, 100.0f, 0.2f)
    }

    override fun cleanup() {

    }
}