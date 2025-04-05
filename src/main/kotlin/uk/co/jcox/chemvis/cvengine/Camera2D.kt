package uk.co.jcox.chemvis.cvengine

import org.joml.Matrix4f
import org.joml.Vector2f

import org.joml.Vector4f


class Camera2D (
    screenWidth: Int,
    screenHeight: Int,
)  {
    var projection: Matrix4f = Matrix4f()
    //The amount visible on the screen by default is set to 500 units width
    //The amount visible height is calculated
    //This can be changed by "Zooming"
    //Objects can be placed outside this range!
    private var camWidth: Float = 500.0f
    private var camHeight: Float = 500.0f

    init {
        update(screenWidth, screenHeight)
    }


    fun update(screenX: Int, screenY:Int) {
        //Get size of camera
        val sf = camWidth / screenX
        camHeight = sf * screenY

        //Get projection Matrix
        this.projection = Matrix4f().ortho(0.0f, camWidth, 0.0f, camHeight, 0.1f, 100.0f)
    }

    fun screenToWorld(screen: Vector2f): Vector2f {
        val inverse = projection.invert(Matrix4f())
        //Z = 0 - Depth does not matter so it can be anything
        //W = 1 - Indicate the vector is a point
        val screenSpace = Vector4f(screen, 0.0f, 1.0f)
        screenSpace.mul(inverse)
        return Vector2f(screenSpace.x, screenSpace.y)
    }

    fun worldToScreen(world: Vector2f): Vector2f {
        val worldSpace = Vector4f(world, 0.0f, 1.0f)
        worldSpace.mul(projection)
        return Vector2f(worldSpace.x, worldSpace.y)
    }
}