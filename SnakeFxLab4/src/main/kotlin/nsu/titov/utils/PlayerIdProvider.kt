package nsu.titov.utils

object PlayerIdProvider {
    private var nextPlayerId: Int = 1

    @Synchronized
    fun getNextPlayerId(): Int {
        nextPlayerId++
        return nextPlayerId - 1
    }
}