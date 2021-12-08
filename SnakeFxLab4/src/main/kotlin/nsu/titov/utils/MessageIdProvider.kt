package nsu.titov.utils

object MessageIdProvider {
    private var nextMessageId: Long = 0L;

    @Synchronized
    fun getNextMessageId(): Long {
        nextMessageId++
        return nextMessageId - 1L
    }
}