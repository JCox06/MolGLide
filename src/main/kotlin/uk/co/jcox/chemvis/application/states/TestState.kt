package uk.co.jcox.chemvis.application.states

import uk.co.jcox.chemvis.cvengine.IApplicationState
import uk.co.jcox.chemvis.cvengine.InputManager
import uk.co.jcox.chemvis.cvengine.scenegraph.EntityLevel
import uk.co.jcox.chemvis.cvengine.scenegraph.TextComponent
import uk.co.jcox.chemvis.cvengine.scenegraph.TransformComponent

class TestState : IApplicationState {

    private lateinit var levelRoot: EntityLevel

    override fun init() {
        levelRoot = EntityLevel()
        levelRoot.addComponent(TransformComponent(0.0f, 0.0f, 0.0f))

        //Add some text at the centre of the screen
        val myTextEntity = levelRoot.addEntity()
        myTextEntity.addComponent(TextComponent("Hello!", "", 1.0f, 1.0f, 1.0f, 0.1f))
    }

    override fun update(inputManager: InputManager?) {

    }

    override fun render() {

    }

    override fun cleanup() {

    }
}