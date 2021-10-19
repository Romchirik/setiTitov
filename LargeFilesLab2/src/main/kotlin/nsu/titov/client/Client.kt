package nsu.titov.client

import mu.KotlinLogging
import nsu.titov.myproto.Message
import nsu.titov.myproto.Message.Companion.readMessage
import nsu.titov.myproto.Message.Companion.sendMessage
import nsu.titov.myproto.MessageType
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.io.RandomAccessFile
import java.net.Socket

class Client(
    serverIp: String,
    serverPort: Int,
    private val file: File
) : Runnable {
    private val logger = KotlinLogging.logger {}

    private lateinit var inputStream: InputStream
    private lateinit var outputStream: OutputStream
    private var socket: Socket = Socket(serverIp, serverPort)


    override fun run() {
        logger.info { "Started as client address: ${socket.inetAddress.hostAddress} port: ${socket.port}" }
        outputStream = socket.getOutputStream()
        inputStream = socket.getInputStream()

        if (!initTransmitting()) {
            logger.warn { "Unable to start transmitting" }
            return
        }

        val randomAccessFile = RandomAccessFile(file, "r")

        while (randomAccessFile.length() != randomAccessFile.filePointer) {
            val buffer = ByteArray(Message.MAX_PAYLOAD_SIZE)
            val totalBytesRead = randomAccessFile.read(buffer)
            val message = Message(
                type = MessageType.DATA,
                payload = buffer.copyOfRange(0, totalBytesRead)
            )
            sendMessage(message, outputStream, logger)
            Thread.sleep(1000)
        }
        sendMessage(Message(type = MessageType.FINISH), outputStream, logger)

        val finishMessage = readMessage(inputStream, logger)
        if (finishMessage.type == MessageType.ERROR) {
            logger.info { "Transmitting failed" }
        } else {
            logger.info { "Transmitting successful" }
        }

        randomAccessFile.close()
        socket.close()
    }

    private fun initTransmitting(): Boolean {
        logger.debug { "Initializing connection" }

        val message = Message(
            type = MessageType.INIT,
            filename = file.name,
            fileSize = file.length()
        )
        sendMessage(message, outputStream, logger)

        val response = readMessage(inputStream, logger)

        return when (response.type) {
            MessageType.ACCEPT -> {
                logger.debug { "Connection initialized" }
                true
            }
            else -> {
                logger.debug { "Failed to initialize connection, server rejected connection" }
                false
            }
        }

    }


}