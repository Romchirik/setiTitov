package nsu.titov.client

import nsu.titov.proto.SnakeProto

data class AnnounceItem(
    var players: List<SnakeProto.GamePlayer>,
    var config: SnakeProto.GameConfig,
    var canJoin: Boolean = true
)
