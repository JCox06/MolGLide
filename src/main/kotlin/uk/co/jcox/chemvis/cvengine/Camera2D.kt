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


    fun screenToWorld(screenPos: Vector4fc) : Vector4f {
        val viewSpace = screenToView(screenPos)
        val worldSpace = viewSpacetoWorldSpace(viewSpace)
        return worldSpace
    }


    fun screenToView(screenPos: Vector4fc) : Vector4f {
        //Screen -> Clip Space -> View Space

        //1) Convert to clip space
        val mscreen = Vector4f(screenPos.x(), screenHeight - screenPos.y(), screenPos.z(), screenPos.w())
        val clipSpace = screenToClipSpace(mscreen)

        //2) Convert to view Space
        val viewSpace = clipSpaceToViewSpace(clipSpace)

        return viewSpace
    }


    private fun viewSpacetoWorldSpace(viewSpace: Vector4fc) : Vector4f {
        val viewSpaceInverse = lookAt.invert()
        val worldSpace = viewSpace.mul(viewSpaceInverse, Vector4f())
        return worldSpace
    }

    private fun clipSpaceToViewSpace(clipSpace: Vector4fc): Vector4f {
        val viewSpaceInverse = projection.invert(Matrix4f())
        val viewSpace = clipSpace.mul(viewSpaceInverse, Vector4f())
        return viewSpace
    }

    //Take a coordinate from the window and turn it into normalised device coordinates
    private fun screenToClipSpace(screenPos: Vector4fc): Vector4f {
        val clipSpace = Vector4f()
        clipSpace.x = ((screenPos.x() / screenWidth) * 2) - 1
        clipSpace.y = ((screenPos.y() / screenHeight) * 2) - 1
        clipSpace.z = screenPos.z()
        clipSpace.w = screenPos.w()

        return clipSpace
    }

}