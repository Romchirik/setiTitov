package nsu.titov.core

import mu.KotlinLogging
import nsu.titov.global.GlobalConfigImmutable
import nsu.titov.proto.SnakeProto
import nsu.titov.utils.GameStateIdProvider

class SnakeGame : Game {
    private val logger = KotlinLogging.logger {}


    private var fieldConfig: FieldConfig = FieldConfig();
    private lateinit var globalConfigDump: GlobalConfigImmutable

    private val players: Map<Int, PlayerWrapper> = HashMap()
    private val snakes: Map<Int, Snake> = HashMap()

    override fun putTurn(direction: SnakeProto.Direction, id: Int) {
        players[id]?.nextTurn = direction
    }

    override fun startGame(): SnakeProto.GameState {
        TODO("Not yet implemented")
    }


    /**
     *  Game cycle:
     *  1. Move snakes
     *  2. Check collisions
     *  3. Generate new foods
     *  4. Return new result
     */
    override fun tick(): SnakeProto.GameState {
        logger.debug { "Updating game state" }

        snakes.forEach { (_, snake) -> snake.tick() }
        snakes.forEach { (_, snake) -> fieldConfig.normalizeSnake(snake) }

        //on collisions count

        return buildGameState()
    }

    private fun buildGameState(): SnakeProto.GameState {
        logger.debug { "Building new game state" }
        val gameStateBuilder = SnakeProto.GameState.newBuilder()


        snakes.forEach { (id, snake) ->
            gameStateBuilder.addSnakes(
                snake.toProto(fieldConfig)
                    .setPlayerId(id)
                    .setState(SnakeProto.GameState.Snake.SnakeState.ALIVE)
                    .build()
            )
        }

        players.forEach{ (id, player) ->
            SnakeProto.GamePlayer.newBuilder()
                .setId(id)
                .set
        }
        return gameStateBuilder
            .setConfig(SnakeProto.GameConfig.newBuilder().build())
            .setStateOrder(GameStateIdProvider.getNextStateId()).build()
    }

    override fun setConfig(config: GlobalConfigImmutable) {
        globalConfigDump = config

        fieldConfig.height = config.height
        fieldConfig.width = config.width
    }

    private data class PlayerWrapper(
        var nextTurn: SnakeProto.Direction,
        val id: Int,
        val PlayerType: SnakeProto.PlayerType = SnakeProto.PlayerType.HUMAN
    )
}