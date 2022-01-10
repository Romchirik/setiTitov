package nsu.titov.core.data

import nsu.titov.proto.SnakeProto

data class PlayerWrapper(
    val id: Int,
    var playerType: SnakeProto.PlayerType = SnakeProto.PlayerType.HUMAN,
    var lastTurn: SnakeProto.Direction,
    var score: Int = 0
)
