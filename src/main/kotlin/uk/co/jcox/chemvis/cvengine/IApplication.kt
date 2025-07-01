package uk.co.jcox.chemvis.cvengine

/**
 * The CVEngine can use this interface to start the application
 * Only one IApplication can exist throughout CVEngine's lifetime
 * The IApplication should also create a main ApplicationState and bind it to the main window render target
 */
interface IApplication {

    fun init(engineServices: ICVServices)
    fun loop()
    fun cleanup()

}