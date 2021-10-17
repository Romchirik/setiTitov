package nsu.titov.server

import mu.KotlinLogging
import nsu.titov.myproto.Message
import nsu.titov.myproto.MessageType
import nsu.titov.utils.Settings.SPEED_MEASURE_PERIOD_SEC
import nsu.titov.utils.UtilsConverters
import java.io.*
import java.net.Socket
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit


class ClientHandler(
    private val client: Socket,
    private val saveDir: String,
    private val id: Int,
    private val startTime: Long
) : Runnable {
    private lateinit var incomingFile: File
    private lateinit var incomingFileWrapper: RandomAccessFile

    private val logger = KotlinLogging.logger {}

    private var inputStream: InputStream = client.getInputStream()
    private var outputStream: OutputStream = client.getOutputStream()

    private var incFileSize: Long = 0L

    private var totalBytesRead: Long = 0
    private var lastBytesRead: Long = 0

    private var speedCounter: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()

    init {
        speedCounter.scheduleAtFixedRate({
            val avgSpeed = (totalBytesRead / ((System.currentTimeMillis() - startTime) / 1000.0)).toInt()
            val instSpeed = lastBytesRead / SPEED_MEASURE_PERIOD_SEC
            lastBytesRead = 0
            logger.info { "Client $id, avgSpeed: $avgSpeed B/s, instSpeed: $instSpeed B/s" }
        }, SPEED_MEASURE_PERIOD_SEC, SPEED_MEASURE_PERIOD_SEC, TimeUnit.SECONDS)
    }

    override fun run() {
        var stopReceiving = false
        try {
            initTransmitting()
        } catch (e: Throwable) {
            logger.error { "Unable to init transmitting: $e" }
            return
        }
        logger.info { "New client accepted, id: $id" }

        while (!stopReceiving) {
            val incMessage = readMessage()
            when (incMessage.type) {
                MessageType.ERROR -> {
                    shutdown()
                    return
                }
                MessageType.FINISH -> {
                    stopReceiving = true
                }
                MessageType.DATA -> {
                    totalBytesRead += incMessage.getPayload()!!.size
                    lastBytesRead += incMessage.getPayload()!!.size
                    incomingFileWrapper.write(incMessage.getPayload()!!)
                }
                else -> {
                    shutdown()
                    return
                }
            }


        }
        if (totalBytesRead != incFileSize) {
            sendMessage(Message(type = MessageType.ERROR))
            incomingFileWrapper.close()
            if (incomingFile.exists()) {
                incomingFile.delete()
            }
            client.close()
            return
        } else {
            sendMessage(Message(type = MessageType.SUCCESS))
        }

        incomingFileWrapper.close()
        speedCounter.shutdown()
        client.close()
        logger.info { "Client: $id, transmitting successful, saved to ${incomingFile.path}" }
    }

    private fun initTransmitting(): Boolean {
        logger.debug { "Initializing connection" }

        val initMessage = readMessage()
        if (initMessage.type != MessageType.INIT) {
            logger.debug { "Failed to initialize connection, rejecting connection" }
            shutdown()
        }

        val response = Message(type = MessageType.ACCEPT)
        sendMessage(response)

        incomingFile = File(
            StringBuilder()
                .append("./")
                .append(saveDir)
                .append("/")
                .append(File(initMessage.getFileName()!!).name).toString()
        )
        incomingFile.createNewFile()
        incomingFileWrapper = RandomAccessFile(incomingFile, "rw")
        incFileSize = initMessage.getFileSize()!!

        logger.debug { "Connection initialized" }
        return true
    }

    private fun readMessage(): Message {
        try {
            val size = inputStream.readNBytes(Int.SIZE_BYTES)
            val rawMessage = inputStream.readNBytes(UtilsConverters.bytesToInt(size))
            val tmp = Message.deserialize(rawMessage)
            logger.debug { "Client: $id, received message, type: ${tmp.type}" }
            return tmp
        } catch (e: Throwable) {
            logger.error { "Client: $id, error occurred while receiving the message: $e" }
            return Message(type = MessageType.ERROR)
        }
    }

    private fun sendMessage(message: Message): Boolean {
        logger.debug { "Client: $id, sending message, type: ${message.type}" }
        return try {
            val rawMessage = Message.serialize(message)
            outputStream.write(UtilsConverters.intToBytes(rawMessage.size))
            outputStream.write(rawMessage)
            true
        } catch (e: Throwable) {
            logger.error { "Error occurred while sending the message: $e" }
            false
        }
    }


    /**
     * emergency method, terminates handler immediately
     */
    private fun shutdown() {
        logger.warn { "Shutting down the client" }

        try {
            sendMessage(Message(type = MessageType.ERROR))
        } catch (e: IOException) {
            logger.warn { "Unable to close connection properly" }
        }
        client.close()
        incomingFileWrapper.close()
        if (incomingFile.exists()) {
            incomingFile.delete()
        }
        speedCounter.shutdown()
    }


}