package nsu.titov.client

object StateProvider {
    private val state = ClientState()

    fun getState(): ClientState {
        return state
    }
}