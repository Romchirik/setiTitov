import org.junit.Test;

import java.util.UUID;

public class MessagesTest {

    @Test
    void messageTest() {
        var uuid = UUID.randomUUID();
        var payload = "Aboba";
        var type = MessageType.DATA;

        var message = MessageBuilderUtil.INSTANCE.buildMessage(
                payload,
                uuid,
                type
        );

        byte[] tmp = MessageBuilderUtil.INSTANCE.serializeMessage(message);



    }
}
