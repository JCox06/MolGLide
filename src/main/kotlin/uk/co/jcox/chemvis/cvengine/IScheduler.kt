package uk.co.jcox.chemvis.cvengine

interface IScheduler {

    fun runAsync(runnable: Runnable)

    fun runSync(runnable: Runnable)
}