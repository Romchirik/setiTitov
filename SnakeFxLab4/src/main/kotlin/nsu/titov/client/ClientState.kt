package nsu.titov.client

import nsu.titov.proto.SnakeProto
import java.net.InetAddress

data class ClientState(
    var id: Int = 0,
    var role: SnakeProto.NodeRole = SnakeProto.NodeRole.VIEWER,
    var serverAddress: InetAddress = InetAddress.getLocalHost(),
    var serverPort: Int = 0,
    var lastGameState: SnakeProto.GameState? = null
)
