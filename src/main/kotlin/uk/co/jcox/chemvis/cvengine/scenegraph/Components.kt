package uk.co.jcox.chemvis.cvengine.scenegraph

import kotlin.reflect.KClass


interface IComponent

class TransformComponent (
    val x: Float,
    val y: Float,
    val z: Float,
) : IComponent

class TextComponent (
    val text: String,
    val bitmapFont: String,
    val colourX: Float,
    val colourY: Float,
    val colourZ: Float,
    val size: Float,
) : IComponent


class IdGen {
    var currentID: Int = 0

    fun newID(): Int {
        return ++currentID
    }
}

class EntityLevel (
    private val idGen: IdGen = IdGen(),
    val id: Int = idGen.currentID,
    val parent: EntityLevel? = null
) {
    private val components: MutableMap<KClass<out IComponent>, IComponent> = mutableMapOf()
    private val children: MutableMap<Int, EntityLevel> = mutableMapOf()

    fun addComponent(component: IComponent) {
        components[component::class] = component
    }

    fun <T: IComponent> getComponent(componentClass: KClass<T>) : T {
        return components[componentClass] as T
    }
    fun addEntity () : EntityLevel {
        val newID = idGen.newID()
        val entity = EntityLevel(idGen, newID, this)
        children[newID] = entity
        return entity
    }

    fun getChild(id: Int) : EntityLevel? {
        return children[id]
    }
}