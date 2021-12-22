package nsu.titov.utils

object MessageIdProvider {
    private var nextMessageId: Long = 0L;

    @Synchronized
    fun getNextMessageId(): Long {
        nextMessageId++
        return nextMessageId - 1L
    }

    @Synchronized
    fun getLocalProvider(initialId: Long): LocalMessageIdProvider {
        return LocalMessageIdProvider(initialId)
    }

    class LocalMessageIdProvider {
        private var nextMessageId: Long = 0L;

        constructor(initialId: Long) {

        }
    }
}