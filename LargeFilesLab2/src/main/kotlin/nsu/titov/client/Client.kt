package nsu.titov.client

import mu.KotlinLogging
import nsu.titov.myproto.Message
import nsu.titov.myproto.MessageType
import nsu.titov.utils.UtilsConverters
import java.io.*
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
            sendMessage(message)
            Thread.sleep(1000)
        }
        sendMessage(Message(type = MessageType.FINISH))

        val finishMessage = readMessage()
        if (finishMessage.type == MessageType.ERROR) {
            logger.info { "Transmitting failed" }
        } else {
            logger.info { "Transmitting successful" }
        }

        randomAccessFile.close()
        socket.close()
    }


    private fun readMessage(): Message {
        return try {
            val size = inputStream.readNBytes(Int.SIZE_BYTES)
            val rawMessage = inputStream.readNBytes(UtilsConverters.bytesToInt(size))
            Message.deserialize(rawMessage)
        } catch (e: Throwable) {
            Message(type = MessageType.ERROR)
        }
    }

    private fun sendMessage(message: Message): Boolean {
        return try {
            val rawMessage = Message.serialize(message)
            outputStream.write(UtilsConverters.intToBytes(rawMessage.size))
            outputStream.write(rawMessage)
            true
        } catch (e: IOException) {
            false
        }
    }

    private fun initTransmitting(): Boolean {
        val message = Message(
            type = MessageType.INIT,
            filename = file.name,
            fileSize = file.length()
        )
        sendMessage(message)

        val response = readMessage()

        return when (response.type) {
            MessageType.ACCEPT -> true
            else -> false
        }
    }


}