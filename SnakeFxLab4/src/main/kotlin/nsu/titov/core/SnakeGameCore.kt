package nsu.titov.core

import mu.KotlinLogging
import nsu.titov.core.data.CoreConfig
import nsu.titov.core.data.PlayerWrapper
import nsu.titov.core.data.Playfield
import nsu.titov.core.data.Point
import nsu.titov.proto.SnakeProto

class SnakeGameCore(private val config: CoreConfig) : GameCore {
    private val logger = KotlinLogging.logger {}

    //entities
    private val snakes: MutableMap<Int, Snake> = HashMap()
    private val players: MutableMap<Int, PlayerWrapper> = HashMap()
    private val foods: MutableList<Point> = ArrayList()
    private val playfield: Playfield = Playfield(config.width, config.height)

    private var targetFoodCount = config.foodStatic + players.size * config.foodPerPlayer

    /**
     *  Game cycle:
     *  1. Move snakes
     *  2. Check collisions
     *  3. Generate new foods
     *  4. Return new result
     */
    override fun tick() {
//        logger.debug { "Updating game state" }
        targetFoodCount = config.foodStatic + players.size * config.foodPerPlayer

        players.forEach { (id, player) -> snakes[id]?.direction = player.lastTurn }
        snakes.forEach { (_, snake) -> snake.tick() }

        val removeIds: MutableSet<Int> = HashSet()
        snakes.forEach { (id0, snake0) ->
            snakes.forEach { (id1, snake1) ->
                if (id0 != id1 && snake0.ifCollide(snake1.getHead())) {
                    removeIds.add(id1)
                }
            }
        }


        snakes.forEach { (_, snake) ->


        }


    }

    private fun checkFree(point: Point): Boolean {
        return true;
    }

    override fun putTurn(id: Int, dir: SnakeProto.Direction) {
        players[id]?.let { wrapper -> wrapper.lastTurn = dir }
            ?: logger.error { "Player with id: $id not found, unable put next turn" }
    }

    //TODO доделать адекватное добавление игрока
    override fun addPlayer(id: Int, playerType: SnakeProto.PlayerType): Boolean {
        val player =
            PlayerWrapper(id = id, playerType = playerType, lastTurn = SnakeProto.Direction.DOWN)
        if (players[player.id] != null) {
            logger.warn { "Trying to add player with existing id: ${player.id}" }
            return false
        }

        //TODO trying to find free place
        players[player.id] = player
        //TODO add new snake remove playfield((
        snakes[player.id] = Snake(Point(0, 7), Point(0, -7), playfield)
        return true
    }

    /** Removes player from players list, but not his snake, snake state changes to ZOMBIE and removes only when snake
     * dies
     * @param id player to remove
     */
    override fun removePlayer(id: Int) {
        players.remove(id) ?: logger.error { "Player with id: $id not found, unable to remove player" }
        snakes[id]?.setStateZombie()
            ?: logger.error { "Snake for player with id: $id not found!, unable to remove snake" }
    }

    override fun getPlayers(): Map<Int, PlayerWrapper> {
        return players
    }

    override fun getFoods(): List<Point> {
        return foods
    }

    override fun getSnakes(): Map<Int, Snake> {
        return snakes
    }
}