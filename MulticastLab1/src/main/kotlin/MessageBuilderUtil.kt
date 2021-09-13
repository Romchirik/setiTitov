import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.util.*

object MessageBuilderUtil {

    fun buildMessage(message: ByteArray): Message {
        val uuid = UUID.fromString(message.copyOfRange(0, Constants.UUID_LENGTH).toString(Charset.forName("UTF8")))
        val type = MessageType.fromInt(message[Constants.MESSAGE_SIZE - 1].toInt())
        return Message(uuid, type)
    }

    fun serializeMessage(message: Message): ByteArray {
        val buffer = ByteBuffer.allocate(Constants.MESSAGE_SIZE);
        buffer.put(message.uuid.toString().toByteArray(Charset.forName("UTF8")));
        buffer.putInt(message.type.value)
        return buffer.array()
    }
}