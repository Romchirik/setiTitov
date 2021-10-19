package nsu.titov.myproto

import mu.KLogger
import nsu.titov.utils.UtilsConverters
import java.io.InputStream
import java.io.OutputStream
import java.nio.ByteBuffer


data class Message(
    val type: MessageType,
    private var fileSize: Long? = null,
    private var filename: String? = null,
    private var payload: ByteArray? = null
) {

    fun getFileSize(): Long? {
        return fileSize
    }

    fun getFileName(): String? {
        return filename
    }

    fun getPayload(): ByteArray? {
        return payload
    }

    companion object {
        const val MAX_PAYLOAD_SIZE = 32000
        const val MAX_NAME_SIZE = 4096

        fun readMessage(inputStream: InputStream, logger: KLogger): Message {
            try {
                val size = inputStream.readNBytes(Int.SIZE_BYTES)
                val rawMessage = inputStream.readNBytes(UtilsConverters.bytesToInt(size))
                val tmp = deserialize(rawMessage)
                logger.debug { "Received message, type: ${tmp.type}" }
                return tmp
            } catch (e: Throwable) {
                logger.error { "Error occurred while receiving the message: $e" }
                return Message(type = MessageType.ERROR)
            }
        }

        fun sendMessage(message: Message, outputStream: OutputStream, logger: KLogger): Boolean {
            logger.debug { "Sending message, type: ${message.type}" }
            return try {
                val rawMessage = serialize(message)
                outputStream.write(UtilsConverters.intToBytes(rawMessage.size))
                outputStream.write(rawMessage)
                true
            } catch (e: Throwable) {
                logger.error { "Error occurred while sending the message: $e" }
                false
            }
        }

        private fun serialize(message: Message): ByteArray {
            when (message.type) {
                MessageType.INIT -> {
                    val buffer = ByteBuffer.allocate(Byte.SIZE_BYTES + ULong.SIZE_BYTES + message.filename!!.length)
                    buffer
                        .put(message.type.numericCode)
                        .putLong(message.fileSize!!)
                        .put(message.filename!!.toString().toByteArray(Charsets.UTF_8))
                    return buffer.array()
                }
                MessageType.DATA -> {
                    val buffer = ByteBuffer.allocate(Byte.SIZE_BYTES + message.payload!!.size)
                    buffer
                        .put(message.type.numericCode)
                        .put(message.payload)
                    return buffer.array()
                }
                else -> {
                    val buffer = ByteArray(1)
                    buffer[0] = message.type.numericCode
                    return buffer
                }
            }
        }

        fun deserialize(payload: ByteArray): Message {
            val buffer = ByteBuffer.wrap(payload)
            val type = MessageType.fromByte(buffer.get())

            when (type) {
                MessageType.INIT -> {
                    val fileSize = buffer.long
                    val tmp = ByteArray(buffer.remaining())
                    buffer.get(tmp)
                    val filename = String(tmp, Charsets.UTF_8)
                    return Message(
                        type = type,
                        fileSize = fileSize,
                        filename = filename
                    )
                }
                MessageType.DATA -> {
                    val tmp = ByteArray(buffer.remaining())
                    buffer.get(tmp)
                    return Message(
                        type = type,
                        payload = tmp
                    )
                }
                else -> {
                    return Message(type = type)
                }
            }
        }
    }
}
