package uk.co.jcox.chemvis.cvengine

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
}