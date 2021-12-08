package nsu.titov.utils

object PlayerIdProvider {
    private var nextPlayerId: Long = 0L;

    @Synchronized
    fun getNextPlayerId(): Long {
        nextPlayerId++
        return nextPlayerId - 1L
    }
}