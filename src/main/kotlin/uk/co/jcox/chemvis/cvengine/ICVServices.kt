package uk.co.jcox.chemvis.cvengine

import org.joml.Vector2i

/**
 * This class is implemented by the main engine class (CVEngine)
 * It provies the Application (IApplication) with essential methods and data
 *
 * The engine should manage app states and their associated RenderingTargetContext as well as
 * expose the other main classes such as InputManager and BatchRenderer
 */
interface ICVServices {

    /**
     * @return the height of the main window
     */
    fun windowMetrics(): Vector2i


    /**
     * Binds an application state to a render target
     * @param state the app state to bind
     * @param renderTarget the id of the render target. Use null to apply to the main window render target
     */
    fun setApplicationState(state: ApplicationState, renderTarget: String? = null)

    fun inputs(): InputManager

    fun batchRenderer(): Batch2D

    fun instancedRenderer() : InstancedRenderer

    fun levelRenderer(): LevelRenderer

    fun resourceManager(): IResourceManager

    fun setViewport(a: Int, b: Int, c: Int, d: Int)

    fun shutdown()

    fun pauseAppState(stateID: String)

    fun resumeAppState(stateID: String)

    fun destroyAppState(stateID: String)

    fun getAppStateRenderingContext(stateID: String) : IRenderTargetContext?

    fun getState(stateID: String) : ApplicationState?
}