package uk.co.jcox.chemvis.cvengine.newscene

open class Entity (
    var x: Float = 0.0f,
    var y: Float = 0.0f,
    var z: Float = 0.0f,
) {

    open fun template() : Entity {
        return Entity(x, y, z)
    }
}