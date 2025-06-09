package uk.co.jcox.chemvis.cvengine

interface IApplication {

    fun init(engineServices: ICVServices)
    fun loop()
    fun cleanup()

}