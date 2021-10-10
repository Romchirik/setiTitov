package nsu.titov.server

import nsu.titov.utils.Settings.DEFAULT_SAVE_DIR
import java.io.File
import java.net.ServerSocket
import java.nio.file.Files
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class Server(
    port: Int
) : Runnable {

    private val saveDir: File = File(DEFAULT_SAVE_DIR)
    private val executor: ExecutorService = Executors.newCachedThreadPool()
    private var serverSocket: ServerSocket = ServerSocket(port)
    private var nextId: Int = 0

    init {
        Files.createDirectories(saveDir.toPath())
    }

    override fun run() {
        println("Started as server")
        while (!serverSocket.isClosed) {
            val incomingConnection = serverSocket.accept()
            println("New Client accepted!")
            executor.submit(ClientHandler(incomingConnection, saveDir.name, nextId, System.currentTimeMillis()))
            nextId++
        }
        executor.shutdown()
    }
}