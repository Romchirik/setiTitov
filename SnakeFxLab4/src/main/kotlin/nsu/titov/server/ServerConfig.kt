package nsu.titov.server

data class ServerConfig(
    val fieldWidth: Int,
    val fieldHeight: Int,

    val stateTickDelayMs: Int = 1000,
    val pingDelayMs: Int = 100,
    val timeoutDelayMs: Int = 800
)
