package uk.co.jcox.chemvis.cvengine.scenegraph

import com.github.jsonldjava.utils.Obj
import org.joml.Vector3f
import org.joml.plus
import java.util.UUID
import javax.vecmath.Vector2f
import kotlin.enums.enumEntries
import kotlin.reflect.KClass


interface IComponent {
    fun clone() : IComponent
}

class TransformComponent (
    var x: Float,
    var y: Float,
    var z: Float,
    var scale: Float  = 1.0f,
    var visible: Boolean = true
) : IComponent {
    override fun clone(): IComponent {
        return TransformComponent(x, y, z, scale, visible)
    }
}

class TextComponent (
    var text: String,
    var bitmapFont: String,
    var colourX: Float,
    var colourY: Float,
    var colourZ: Float,
    var scale: Float,
) : IComponent {
    override fun clone(): IComponent {
        return TextComponent(text, bitmapFont, colourX, colourY, colourZ, scale)
    }
}

class ObjComponent (
    var modelGeomID: String,
    //Room to include other things later!
) : IComponent {
    override fun clone(): IComponent {
        return ObjComponent(modelGeomID)
    }
}

class LineDrawerComponent (
    val lineTo: Vector3f,
    val width: Float = 1.0f
) : IComponent {
    override fun clone(): IComponent {
        return LineDrawerComponent(Vector3f(lineTo))
    }
}


class EntityLevel (
    val parent: EntityLevel? = null,
    val id: UUID = UUID.randomUUID()
) {
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

    fun getComponents() : Iterator<IComponent> {
        return components.values.iterator()
    }

    fun addEntity () : EntityLevel {
        val entity = EntityLevel( this)
        children.add(entity)
        return entity
    }

    fun removeEntity(entity: EntityLevel) {
        children.remove(entity)
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
        if (hasComponent(TransformComponent::class)) {
            val transComp = this.getComponent(TransformComponent::class)
            return Vector3f(transComp.x, transComp.y, transComp.z) + getAbsoluteTranslation()
        }

        return getAbsoluteTranslation()
    }

    fun getAbsoluteTranslation() : Vector3f {
        var holder = this

        val pos: Vector3f = Vector3f()

        while (true) {
            if (holder.parent != null) {
                holder = holder.parent
            } else {
                return pos
            }
            if (holder.hasComponent(TransformComponent::class)) {
                val transformComp = holder.getComponent(TransformComponent::class)
                pos.x += transformComp.x
                pos.y += transformComp.y
                pos.z += transformComp.z
            }
        }
    }

    fun clone(copyParent: EntityLevel?) : EntityLevel {

        val currentCopy = EntityLevel(copyParent, this.id)
        getComponents().forEach { comp ->
            currentCopy.addComponent(comp.clone())
        }

        children.forEach { child ->
            val childClone = child.clone(currentCopy)
            currentCopy.children.add(childClone)
        }

        return currentCopy
    }

    override fun toString(): String {
        return "${components.size} components with ${children.size} children"
    }


    fun findByID(id: UUID) : EntityLevel? {
        if (this.id == id) {
            return this
        }

        for (child in children) {
            val found = child.findByID(id)

            if (found != null) {
                return found
            }
        }
        return null
    }
}