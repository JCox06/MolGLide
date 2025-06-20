package uk.co.jcox.chemvis.cvengine

import org.joml.Vector2i

interface ICVServices {
    fun windowMetrics(): Vector2i

    fun setCurrentApplicationState(state: IApplicationState)

    fun inputs(): InputManager

    fun batchRenderer(): Batch2D

    fun instancedRenderer() : InstancedRenderer

    fun levelRenderer(): LevelRenderer

    fun resourceManager(): IResourceManager

    fun setViewport(a: Int, b: Int, c: Int, d: Int)

    fun shutdown()
}