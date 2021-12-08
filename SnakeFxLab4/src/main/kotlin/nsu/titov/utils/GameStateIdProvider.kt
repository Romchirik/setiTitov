package nsu.titov.utils

object GameStateIdProvider {
    private var NextStateId: Int = 0

    @Synchronized
    fun getNextStateId(): Int {
        NextStateId++
        return NextStateId - 1
    }
}