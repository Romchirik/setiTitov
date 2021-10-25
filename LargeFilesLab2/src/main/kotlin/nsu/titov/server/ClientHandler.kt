package nsu.titov.server

import mu.KotlinLogging
import nsu.titov.myproto.Message
import nsu.titov.myproto.Message.Companion.readMessage
import nsu.titov.myproto.Message.Companion.sendMessage
import nsu.titov.myproto.MessageType
import nsu.titov.utils.Settings.SPEED_MEASURE_PERIOD_SEC
import java.io.*
import java.net.Socket
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import kotlin.random.Random
import kotlin.random.nextUInt


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
            val incMessage = readMessage(inputStream, logger)
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
            sendMessage(Message(type = MessageType.ERROR), outputStream, logger)
            incomingFileWrapper.close()
            if (incomingFile.exists()) {
                incomingFile.delete()
            }
            client.close()
            return
        } else {
            sendMessage(Message(type = MessageType.SUCCESS), outputStream, logger)
        }

        incomingFileWrapper.close()
        speedCounter.shutdown()
        client.close()
        logger.info { "Client: $id, transmitting successful, saved to ${incomingFile.path}" }
    }

    private fun initTransmitting(): Boolean {
        logger.debug { "Initializing connection" }

        val initMessage = readMessage(inputStream, logger)
        if (initMessage.type != MessageType.INIT) {
            logger.debug { "Failed to initialize connection, rejecting connection" }
            shutdown()
        }

        val response = Message(type = MessageType.ACCEPT)
        sendMessage(response, outputStream, logger)

        incomingFile = File(
            StringBuilder()
                .append("./")
                .append(saveDir)
                .append("/")
                .append(File(initMessage.getFileName()!!).name).toString()
        )
        val defaultName = incomingFile.name
        if (incomingFile.exists()) {
            do {
                incomingFile = File(getNewName(defaultName))
            } while (incomingFile.exists())
        }
        incomingFile.createNewFile()
        incomingFileWrapper = RandomAccessFile(incomingFile, "rw")
        incFileSize = initMessage.getFileSize()!!

        logger.debug { "Connection initialized" }
        return true
    }


    private fun getNewName(defaultName: String): String {
        return StringBuilder()
            .append("./")
            .append(saveDir)
            .append("/")
            .append(Random.nextUInt())
            .append(defaultName).toString()
    }

    /**
     * emergency method, terminates handler immediately
     */
    private fun shutdown() {
        logger.warn { "Shutting down the client" }

        try {
            sendMessage(Message(type = MessageType.ERROR), outputStream, logger)
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