package nsu.titov.server

data class ServerConfig(
    val stateTickDelayMs: Int = 1000,
    val pingDelayMs: Int = 100,
    val timeoutDelayMs: Int = 800,

    val playfieldHeight: Int = 30,
    val playfieldWidth: Int = 40
)
