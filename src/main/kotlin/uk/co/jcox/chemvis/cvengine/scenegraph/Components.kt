package uk.co.jcox.chemvis.cvengine.scenegraph

import org.joml.Vector3f
import java.util.UUID
import javax.vecmath.Vector2f
import kotlin.reflect.KClass


interface IComponent

class TransformComponent (
    var x: Float,
    var y: Float,
    var z: Float,
    var scale: Float  = 1.0f,
    var visible: Boolean = true
) : IComponent

class TextComponent (
    var text: String,
    var bitmapFont: String,
    var colourX: Float,
    var colourY: Float,
    var colourZ: Float,
    var scale: Float,
) : IComponent

class ObjComponent (
    var modelGeomID: String,
    //Room to include other things later!
) : IComponent


class EntityLevel (
    val parent: EntityLevel? = null
) {
    val id: UUID = UUID.randomUUID()
    private val components: MutableMap<KClass<out IComponent>, IComponent> = mutableMapOf()
    private val children: MutableList<EntityLevel> = mutableListOf()

    fun addComponent(component: IComponent) {
        components[component::class] = component
    }

    fun <T: IComponent> getComponent(componentClass: KClass<T>) : T {
        return components[componentClass] as T
    }

    fun <T: IComponent> hasComponent(componentClass: KClass<T>) : Boolean {
        return components.contains(componentClass)
    }

    fun addEntity () : EntityLevel {
        val entity = EntityLevel( this)
        children.add(entity)
        return entity
    }

    fun getChildren() : List<EntityLevel> {
        return children
    }

    fun traverseFunc(func: (EntityLevel) -> Unit) {
        func(this)
        children.forEach {
            it.traverseFunc(func)
        }
    }

    fun getAbsolutePosition() : Vector3f {

        var holder = this
        val pos: Vector3f = Vector3f()

        while (true) {
            if (holder.hasComponent(TransformComponent::class)) {
                val transformComp = holder.getComponent(TransformComponent::class)
                pos.x += transformComp.x
                pos.y += transformComp.y
                pos.z += transformComp.z
            }

            if (holder.parent != null) {
                holder = holder.parent
            } else {
                return pos
            }
        }
    }
}