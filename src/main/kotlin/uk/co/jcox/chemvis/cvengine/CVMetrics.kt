package uk.co.jcox.chemvis.cvengine

class CVMetrics {

    private var frames = 0
    private var drawCalls = 0

    fun resetMetrics() {
        frames = 0
        drawCalls = 0
    }

    fun completeFrame() {
        frames++
    }

    fun completeDraw() {
        drawCalls++
    }

    fun getAverageDrawPerFrame(): Double {
        return drawCalls / frames.toDouble()
    }
}