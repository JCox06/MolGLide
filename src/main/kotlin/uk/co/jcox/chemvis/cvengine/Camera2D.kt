package uk.co.jcox.chemvis.cvengine

import org.joml.Matrix4f
import org.joml.Vector2f

import org.joml.Vector4f


class Camera2D (
    private var screenWidth: Int,
    private var screenHeight: Int,
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

        this.screenWidth = screenX
        this.screenHeight = screenY

        //Get size of camera
        val sf = camWidth / this.screenWidth
        camHeight = sf * this.screenHeight

        //Get projection Matrix
        this.projection = Matrix4f().ortho(0.0f, camWidth, 0.0f, camHeight, 0.1f, 100.0f)
    }

    fun screenToView(screenPos: Vector2f): Vector2f {

        //WORLD SPACE -> VIEW SPACE -> CLIP SPACE -> SCREEN SPACE
        //Currently World Space = View space

        //Convert screen space to normalised clip space
        val clipSpaceX = ((screenPos.x / screenWidth) * 2) - 1
        val clipSpaceY = ((screenPos.y / screenHeight) * 2)  - 1


        //Convert clip space to view/world space
        val inverse = projection.invert(Matrix4f())
        //Z = 0 - Depth does not matter so it can be anything
        //W = 1 - Indicate the vector is a point
        val screenSpace = Vector4f(clipSpaceX, clipSpaceY, 0.0f, 1.0f)
        screenSpace.mul(inverse)
        return Vector2f(screenSpace.x, camHeight - screenSpace.y)
    }

    fun viewToScreen(world: Vector2f): Vector2f {
        TODO("Implement this method - Not used for anything yet")
    }
}