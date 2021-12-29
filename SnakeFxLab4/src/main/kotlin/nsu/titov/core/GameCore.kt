package nsu.titov.core

import nsu.titov.core.data.PlayerWrapper
import nsu.titov.core.data.Point
import nsu.titov.event.Subscriber
import nsu.titov.proto.SnakeProto

interface GameCore {
    fun tick()
    fun addPlayer(id: Int, playerType: SnakeProto.PlayerType): Boolean
    fun removePlayer(id: Int)
    fun putTurn(id: Int, dir: SnakeProto.Direction)

    fun getPlayers(): Map<Int, PlayerWrapper>
    fun getSnakes(): Map<Int, Snake>
    fun getFoods(): List<Point>
}