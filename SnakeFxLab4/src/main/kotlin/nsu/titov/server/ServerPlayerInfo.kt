package nsu.titov.server

import nsu.titov.proto.SnakeProto
import java.net.InetAddress

class ServerPlayerInfo(
    val name: String = "",
    val id: Int,
    val address: String,
    val port: Int,
    var role: SnakeProto.NodeRole,
    var score: Int = 0,
    val playerType: SnakeProto.PlayerType = SnakeProto.PlayerType.HUMAN,
    var connected: Boolean = false
) {

    val addressInet: InetAddress = if (address.isNotEmpty()) {
        InetAddress.getByName(address)
    } else {
        InetAddress.getLocalHost()
    }

    fun toProto(): SnakeProto.GamePlayer {
        return SnakeProto.GamePlayer.newBuilder()
            .setName(name)
            .setId(id)
            .setIpAddress(address)
            .setPort(port)
            .setRole(role)
            .setScore(score)
            .setType(playerType).build()
    }

}
