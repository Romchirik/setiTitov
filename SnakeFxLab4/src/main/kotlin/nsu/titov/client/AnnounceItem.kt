package nsu.titov.client

import nsu.titov.proto.SnakeProto

data class AnnounceItem(
    val players: List<SnakeProto.GamePlayer>,
    val config: SnakeProto.GameConfig,
    val canJoin: Boolean,
    val ip: String
) {
    override fun toString(): String {
        return "$ip ${if (canJoin) "available" else "no places"}"
    }
}
