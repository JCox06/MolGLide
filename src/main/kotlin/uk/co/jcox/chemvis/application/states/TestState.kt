package uk.co.jcox.chemvis.application.states

import org.apache.jena.vocabulary.OWLTest.level
import uk.co.jcox.chemvis.application.ChemVis
import uk.co.jcox.chemvis.cvengine.Camera2D
import uk.co.jcox.chemvis.cvengine.IApplicationState
import uk.co.jcox.chemvis.cvengine.InputManager
import uk.co.jcox.chemvis.cvengine.LevelRenderer
import uk.co.jcox.chemvis.cvengine.scenegraph.EntityLevel
import uk.co.jcox.chemvis.cvengine.scenegraph.TextComponent
import uk.co.jcox.chemvis.cvengine.scenegraph.TransformComponent

class TestState (
    val levelRenderer: LevelRenderer,
    val camera2D: Camera2D
) : IApplicationState {

    private lateinit var levelRoot: EntityLevel

    override fun init() {
        levelRoot = EntityLevel()
        levelRoot.addComponent(TransformComponent(0.0f, 0.0f, 0.0f))

        //Add some text at the centre of the screen
        val myTextEntity = levelRoot.addEntity()
        myTextEntity.addComponent(TextComponent("HELLOFGSDFFSDFHJKSDFHJKSDHJKF", ChemVis.FONT, 1.0f, 1.0f, 1.0f, 0.1f))
        myTextEntity.addComponent(TransformComponent(10.0f, 10.0f, 0.0f))
    }

    override fun update(inputManager: InputManager?) {
    }

    override fun render() {
        levelRenderer.renderLevel(levelRoot, camera2D)
    }

    override fun cleanup() {

    }
}