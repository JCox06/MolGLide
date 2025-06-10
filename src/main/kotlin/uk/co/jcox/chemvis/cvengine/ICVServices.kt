package uk.co.jcox.chemvis.cvengine

import org.joml.Vector2i

interface ICVServices {
    open fun windowMetrics(): Vector2i

    open fun setCurrentApplicationState(state: IApplicationState)

    open fun inputs(): InputManager

    open fun renderer(): Batch2D

    open fun levelRenderer(): LevelRenderer

    open fun resourceManager(): IResourceManager

    open fun toggleDebugPanel()

    open fun setViewport(a: Int, b: Int, c: Int, d: Int)

    open fun shutdown()
}