package uk.co.jcox.chemvis.cvengine

import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.Executors


class CVScheduler : AutoCloseable, IScheduler {
    private val executor = Executors.newFixedThreadPool(4)
    private val syncTasks = ConcurrentLinkedQueue<Runnable>()

    override fun runAsync(runnable: Runnable) {
        executor.submit(runnable)
    }

    override fun runSync(runnable: Runnable) {
        syncTasks.add(runnable)
    }

    fun runAwaitingSyncTasks() {
        val task = syncTasks.poll()
        task?.run()
    }

    override fun close() {
        executor.shutdownNow()
    }
}