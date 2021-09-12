import java.io.InputStreamReader
import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.util.*

object MessageBuilderUtil {


    fun serializeMessage(message: Message): ByteArray {
        val arr = ByteBuffer.allocate(message.length)
        arr.putInt(message.length)
        arr.putLong(message.uid.mostSignificantBits)
        arr.putLong(message.uid.leastSignificantBits)
        arr.put(message.payload.toByteArray(Charset.forName("UTF8")))
        return arr.array()
    }


    fun buildMessage(payload: String, uuid: UUID, type: MessageType): Message {
        return Message(
            payload = payload,
            uid = uuid,
            type = type,
            length = payload.length + Constants.UUID_LENGTH + Constants.MESSAGE_TYPE_LENGTH + Constants.MESSAGE_LENGTH_LENGTH
        )
    }


}