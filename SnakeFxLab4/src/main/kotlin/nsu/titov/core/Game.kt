package nsu.titov.core

import nsu.titov.global.GlobalConfigImmutable
import nsu.titov.proto.SnakeProto

interface Game {
    fun startGame(): SnakeProto.GameState
    fun tick(): SnakeProto.GameState
    fun putTurn(direction: SnakeProto.Direction, id: Int)
    fun setConfig(config: GlobalConfigImmutable)
}