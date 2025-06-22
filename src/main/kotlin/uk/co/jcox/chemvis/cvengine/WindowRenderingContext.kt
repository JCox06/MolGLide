package uk.co.jcox.chemvis.cvengine

import org.joml.Vector2f
import org.joml.Vector2fc
import org.joml.Vector3f
import org.joml.Vector3fc

class WindowRenderingContext(val services: ICVServices) : IRenderTargetContext {

    var iwidth = 1.0f
    var iheight = 1.0f


    override fun recalculate() {
        val wm = services.windowMetrics()

        iwidth = wm.x.toFloat()
        iheight = wm.y.toFloat()
    }

    override fun getHeight(): Float {
        return iheight
    }

    override fun getWidth(): Float {
        return iwidth
    }


    override fun setImGuiWinMetrics(windowPos: Vector2f) {
        TODO("Not yet implemented")
    }

    override fun getMousePos(inputManager: InputManager): Vector2f {
        return inputManager.mousePos()
    }
}