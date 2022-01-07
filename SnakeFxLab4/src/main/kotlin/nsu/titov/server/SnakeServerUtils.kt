package nsu.titov.server

import mu.KotlinLogging
import nsu.titov.client.StateProvider
import nsu.titov.proto.SnakeProto

object SnakeServerUtils {
    private val logger = KotlinLogging.logger {}
    private var serverThread: Thread? = null
    private var server: Server? = null

    @Synchronized
    fun isRunning(): Boolean {
        return serverThread != null
    }

    @Synchronized
    fun getPort(): Int {
        return server?.getPort() ?: -1
    }

    @Synchronized
    fun startServer(serverConfig: ServerConfig) {
        server = SnakeServer(serverConfig)
        serverThread = Thread(server, "Snake server")
        serverThread?.start()
    }

    @Synchronized
    fun restoreServer(message: SnakeProto.GameState) {
        server = SnakeServer.fromProto(message, StateProvider.getState().id)
        serverThread = Thread(server, "Snake server")
        serverThread?.start()
    }

    @Synchronized
    fun stopServer() {
        if (serverThread != null) {
            server?.stop()
            serverThread?.join()
            logger.info { "Server stopped successfully" }
        }
    }
}