package uk.co.jcox.chemvis.cvengine.scenegraph

import org.joml.Vector3f
import org.joml.plus
import java.util.UUID
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
    var bitmapFont: String? =null,
    var colourX: Float? = null,
    var colourY: Float? = null,
    var colourZ: Float? = null,
    var scale: Float? = null,
) : IComponent {
    override fun clone(): IComponent {
        return TextComponent(text, bitmapFont, colourX, colourY, colourZ, scale)
    }
}

class ObjComponent (
    var modelGeomID: String,
    var materialID: String,
    //Room to include other things later!
) : IComponent {
    override fun clone(): IComponent {
        return ObjComponent(modelGeomID, materialID)
    }
}

class LineDrawerComponent (
    val fromCompA: UUID,
    val toCompB: UUID,
    var width: Float? = null,
    var colourX: Float? = null,
    var colourY: Float? = null,
    var colourZ: Float? = null,
) : IComponent {
    override fun clone(): IComponent {
        return LineDrawerComponent(fromCompA, toCompB, width, colourX, colourY, colourZ)
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

    fun <T: IComponent> removeComponent(componentClass: KClass<T>){
        components.remove(componentClass)
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


    //Returns the position of the entity relative to the root node
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

    //Returns the text data as a text component.
    //All properties that were null are resolved in the parent as a cascade
    fun getAbsoluteText(): TextComponent? {

        if (!this.hasComponent(TextComponent::class)) {
            return null
        }

        val ownTextComp = this.getComponent(TextComponent::class)

        val textComp = ownTextComp.clone() as TextComponent

        var holder = this.parent

        while (holder != null) {
            if (holder.hasComponent(TextComponent::class)) {

                val parentComp = holder.getComponent(TextComponent::class)
                if (textComp.bitmapFont == null) {
                   textComp.bitmapFont =  parentComp.bitmapFont
                }

                if (textComp.colourX == null) {
                    textComp.colourX =  parentComp.colourX
                }

                if (textComp.colourY == null) {
                    textComp.colourY =  parentComp.colourY
                }
                if (textComp.colourZ == null) {
                    textComp.colourZ =  parentComp.colourZ
                }

                if (textComp.scale == null) {
                    textComp.scale = parentComp.scale
                }
            }

            holder = holder.parent
        }
        return textComp
    }


    fun getAbsoluteLineDrawer() : LineDrawerComponent? {
        if (! this.hasComponent(LineDrawerComponent::class)) {
            return null
        }

        val ownLineComp = this.getComponent(LineDrawerComponent::class)
        val lineComp = ownLineComp.clone() as LineDrawerComponent

        var  holder = this.parent

        while (holder != null) {

            if (holder.hasComponent(LineDrawerComponent::class)) {
                val parentComp = holder.getComponent(LineDrawerComponent::class)

                if (lineComp.width == null) {
                    lineComp.width = parentComp.width
                }

                if (lineComp.colourX == null) {
                    lineComp.colourX = parentComp.colourX
                }

                if (lineComp.colourY == null) {
                    lineComp.colourY = parentComp.colourY
                }

                if (lineComp.colourZ == null) {
                    lineComp.colourZ = parentComp.colourZ
                }

            }

            holder = holder.parent
        }

        return lineComp
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

    fun clear() {
        this.children.clear()
        this.components.clear()
    }
}