import nsu.titov.proto.SnakeProto
import org.junit.Test

class GoogleSerializerTest {
    @Test
    fun testMessageSerializing() {
        val message = SnakeProto.GameMessage.newBuilder()
            .setMsgSeq(10)
            .setSteer(
                SnakeProto.GameMessage.SteerMsg.newBuilder()
                    .setDirection(SnakeProto.Direction.UP)
                    .build()
            ).build()

        val decoded = SnakeProto.GameMessage.parseFrom(message.toByteArray());
    }
}