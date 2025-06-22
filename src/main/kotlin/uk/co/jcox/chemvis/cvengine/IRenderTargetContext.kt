package uk.co.jcox.chemvis.cvengine

interface IRenderTargetContext {

    fun getHeight() : Float
    fun getWidth() : Float
    fun recalculate()
}