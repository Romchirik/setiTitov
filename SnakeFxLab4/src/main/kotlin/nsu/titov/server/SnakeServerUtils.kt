package nsu.titov.server

import mu.KotlinLogging

object SnakeServerUtils {
    private val logger = KotlinLogging.logger {}
    private var serverThread: Thread? = null
    private var server: Server? = null


    @Synchronized
    fun startServer(serverConfig: ServerConfig) {
        //TODO getting server config and starting server
        if (serverThread == null) {
            server = SnakeServer(serverConfig)
            serverThread = Thread(server, "Snake server")
            serverThread?.start()
        } else {
            logger.warn { "Server already started, check your code and prevent double starting" }
        }
    }

    @Synchronized
    fun stopServer() {
        if (serverThread == null) {
            logger.info { "Unable to stop server? you don't started one" }
            return
        }
        server?.stop()
        serverThread?.join()
        logger.info { "Server stopped successfully" }
    }
}