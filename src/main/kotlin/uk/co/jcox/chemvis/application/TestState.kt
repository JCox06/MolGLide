package uk.co.jcox.chemvis.application

import org.joml.Math
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
        levelRoot.addComponent(TransformComponent(0.0f, 0.0f, 0.0f, 1.0f))

        //Add some text at the centre of the screen
        val myTextEntity = levelRoot.addEntity()
        myTextEntity.addComponent(TextComponent("TEST - STATE", ChemVis.FONT, 1.0f, 1.0f, 1.0f, ChemVis.GLOBAL_SCALE))
        myTextEntity.addComponent(TransformComponent(10.0f, 10.0f, 0.0f, 1.0f))

        val grandChild = myTextEntity.addEntity()
        grandChild.addComponent(TransformComponent(100.0f, 50.0f, 0.0f, ChemVis.GLOBAL_SCALE))
        grandChild.addComponent(TextComponent("I am the grandchild!", ChemVis.FONT, 1.0f, 0.5f, 0.5f, ChemVis.GLOBAL_SCALE))
    }

    override fun update(inputManager: InputManager, timeElapsed: Float) {
        val transform = levelRoot.getComponent(TransformComponent::class)
        transform.x =  100 * Math.sin(timeElapsed)
        transform.y =  20 * Math.cos(timeElapsed)
    }

    override fun render() {
        levelRenderer.renderLevel(levelRoot, camera2D)
    }

    override fun cleanup() {

    }
}