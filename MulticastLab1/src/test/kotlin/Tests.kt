import MessageBuilderUtil.buildMessage
import MessageBuilderUtil.serializeMessage
import org.junit.Assert
import java.util.*
import kotlin.test.Test

class Tests {
    @Test
    fun testMessages() {
        val uuid = UUID.randomUUID()
        val type = MessageType.LEAVE
        val b = Message(uuid, type)
        val a = serializeMessage(b)
        val (uuid1, type1) = buildMessage(a)

        Assert.assertEquals(uuid, uuid1)
        Assert.assertEquals(type, type1)
    }
}