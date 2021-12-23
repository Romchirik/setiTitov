package nsu.titov.client

import nsu.titov.net.Message
import nsu.titov.proto.SnakeProto
import java.net.InetAddress

data class AnnounceItem(
    val playersCount: Int,
    val canJoin: Boolean,
    val ip: InetAddress
) {
    override fun toString(): String {
        return "${ip.toString().removePrefix("/")} ${if (canJoin) "available" else "no places"}, players: $playersCount"
    }

    companion object {

        @Synchronized
        fun fromProto(message: Message): AnnounceItem {
            assert(message.msg.typeCase != SnakeProto.GameMessage.TypeCase.ANNOUNCEMENT)
            { "Unable to build AnnounceItem from not an announcement message" }


            return AnnounceItem(
                playersCount = message.msg.announcement.players.playersCount,
                canJoin = message.msg.announcement.canJoin,
                ip = message.ip
            )
        }
    }
}
