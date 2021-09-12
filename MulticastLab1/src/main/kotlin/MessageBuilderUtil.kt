import java.nio.ByteBuffer
import java.util.*

object MessageBuilderUtil {

    fun buildMessage(message: ByteArray): Message {
        val mostSigBits = ByteUtils.bytesToLong(message.copyOfRange(0, Constants.UUID_LENGTH / 2))
        val leastSigBits =
            ByteUtils.bytesToLong(message.copyOfRange(Constants.UUID_LENGTH / 2, Constants.UUID_LENGTH))
        val type = MessageType.fromInt(message[Constants.MESSAGE_SIZE - 1].toInt())
        return Message(UUID(mostSigBits, leastSigBits), type)
    }

    fun serializeMessage(message: Message): ByteArray {
        val buffer = ByteBuffer.allocate(Constants.MESSAGE_SIZE);
        buffer.putLong(message.uuid.mostSignificantBits)
        buffer.putLong(message.uuid.leastSignificantBits)
        buffer.putInt(message.type.value)
        return buffer.array()
    }
}