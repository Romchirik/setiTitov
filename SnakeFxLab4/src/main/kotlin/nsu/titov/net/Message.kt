package nsu.titov.net

import nsu.titov.proto.SnakeProto
import java.net.InetAddress

data class Message(
    val msg: SnakeProto.GameMessage,
    val ip: InetAddress,
    val port: Int
)