package nsu.titov.server

import nsu.titov.core.Snake
import nsu.titov.core.data.Point
import nsu.titov.proto.SnakeProto
import nsu.titov.settings.Settings
import nsu.titov.settings.SettingsProvider
import nsu.titov.utils.GameStateIdProvider
import nsu.titov.utils.pointToCoord

class StateBuilder private constructor() {
    private var config: ServerConfig? = null
    private var players: Map<Int, ServerPlayerInfo> = HashMap()
    private var snakes: Map<Int, Snake> = HashMap()
    private var foods: List<Point> = ArrayList()

    fun setFoods(foods: List<Point>): StateBuilder {
        this.foods = foods
        return this
    }

    fun setSnakes(snakes: Map<Int, Snake>): StateBuilder {
        this.snakes = snakes
        return this
    }

    fun setConfig(config: ServerConfig): StateBuilder {
        this.config = config
        return this
    }

    fun setPlayers(players: Map<Int, ServerPlayerInfo>): StateBuilder {
        this.players = players
        return this
    }

    fun build(): SnakeProto.GameMessage.StateMsg {
        assert(config != null) { "Missing server config to build state message" }

        val config = SnakeProto.GameConfig.newBuilder()
            .setHeight(config!!.playfieldHeight)
            .setWidth(config!!.playfieldWidth)
            .setStateDelayMs(SettingsProvider.getSettings().stateTickDelayMs)
            .setNodeTimeoutMs(SettingsProvider.getSettings().timeoutDelayMs)
            .setPingDelayMs(SettingsProvider.getSettings().pingDelayMs)
            .build()

        val state = SnakeProto.GameState.newBuilder()
            .setStateOrder(GameStateIdProvider.getNextStateId())
            .setConfig(config)


        snakes.forEach { (id, snake) ->
            val keyPoints = snake.serialize()
            val snakeMsg = SnakeProto.GameState.Snake.newBuilder()
                .setPlayerId(id)
                .setHeadDirection(snake.direction)
                .setState(snake.getState())


            keyPoints.forEach { point ->
                snakeMsg.addPoints(pointToCoord(point))
            }

            state.addSnakes(snakeMsg.build())
        }
        val playersMsg = SnakeProto.GamePlayers.newBuilder()
        players.forEach { (id, player) ->
            playersMsg.addPlayers(
                SnakeProto.GamePlayer.newBuilder()
                    .setId(id)
                    .setRole(player.role)
                    .setIpAddress(player.address)
                    .setPort(player.port)
                    .setName(player.name)
                    .setScore(player.score).build()
            )
        }
        state.players = playersMsg.build()
        foods.forEach { point ->
            state.addFoods(pointToCoord(point))
        }

        return SnakeProto.GameMessage.StateMsg.newBuilder().setState(state.build()).build()
    }

    companion object {
        fun getBuilder(): StateBuilder {
            return StateBuilder()
        }
    }
}