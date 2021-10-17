package nsu.titov.server

import mu.KotlinLogging
import nsu.titov.utils.Settings.DEFAULT_SAVE_DIR
import java.io.File
import java.net.ServerSocket
import java.nio.file.Files
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class Server(
    private var port: Int
) : Runnable {

    private val saveDir: File = File(DEFAULT_SAVE_DIR)
    private val executor: ExecutorService = Executors.newCachedThreadPool()
    private var serverSocket: ServerSocket = ServerSocket(port)
    private var nextId: Int = 0
    private val logger = KotlinLogging.logger {}

    init {
        Files.createDirectories(saveDir.toPath())
    }

    override fun run() {
        logger.info("Started as server, address: ${serverSocket.inetAddress.hostAddress} port: $port")
        while (!serverSocket.isClosed) {
            val incomingConnection = serverSocket.accept()
            logger.info("New client trying to connect: ${incomingConnection.inetAddress.hostAddress} port: ${incomingConnection.port}")
            executor.submit(ClientHandler(incomingConnection, saveDir.name, nextId, System.currentTimeMillis()))
            logger.info("Given id: $nextId")
            nextId++
        }
        executor.shutdown()
    }
}