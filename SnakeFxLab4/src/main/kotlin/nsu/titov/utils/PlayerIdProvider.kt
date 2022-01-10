package nsu.titov.utils

object PlayerIdProvider {
    private var nextPlayerId: Int = 1

    @Synchronized
    fun setNextPlayerId(id: Int) {
        nextPlayerId = id
    }

    @Synchronized
    fun getNextPlayerId(): Int {
        nextPlayerId++
        return nextPlayerId - 1
    }
}