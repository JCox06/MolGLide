package uk.co.jcox.chemvis.cvengine

import kotlinx.coroutines.CoroutineDispatcher
import org.tinylog.Logger
import java.util.concurrent.ConcurrentLinkedDeque

import kotlin.coroutines.CoroutineContext

class SynchronousTaskDispatcher : CoroutineDispatcher() {

    private val tasks = ConcurrentLinkedDeque<Runnable>()

    override fun dispatch(context: CoroutineContext, block: kotlinx.coroutines.Runnable) {

        Logger.info { "Scheduled Synchronous Task" }

        tasks.add(block)
    }

    fun runJobs() {
        while (tasks.isNotEmpty()) {
            tasks.poll()?.run()
        }
    }
}