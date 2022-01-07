package nsu.titov.utils

object ThreadManager {

    private val threads : MutableList<Thread> = ArrayList()

    @Synchronized
    fun addThread(thread: Thread){
        threads.add(thread)
    }

    @Synchronized
    fun shutdown() {
        threads.forEach { thread ->
            thread.interrupt()
        }
    }
}
