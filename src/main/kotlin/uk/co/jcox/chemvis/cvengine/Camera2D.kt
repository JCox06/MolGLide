package uk.co.jcox.chemvis.cvengine

import Jama.Matrix
import org.joml.*


class Camera2D (
    private var screenWidth: Int,
    private var screenHeight: Int,
)  {
    private var projection: Matrix4f = Matrix4f()
    val cameraPosition: Vector3f = Vector3f(0.0f, 0.0f, 1.0f)
    val cameraDirection: Vector3f = Vector3f(0.0f, 0.0f, -1.0f)
    private var lookAt: Matrix4f = Matrix4f()
    //The amount visible on the screen by default is set to 500 units width
    //The amount visible height is calculated
    //This can be changed by "Zooming"
    //Objects can be placed outside this range!
    var camWidth: Float = 500.0f
    var camHeight: Float = 500.0f

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
        this.lookAt = Matrix4f().lookAt(cameraPosition, cameraPosition + cameraDirection.normalize(), Vector3f(0.0f, 1.0f, 0.0f))
    }


    fun combined(): Matrix4f {
        return projection * lookAt
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

}