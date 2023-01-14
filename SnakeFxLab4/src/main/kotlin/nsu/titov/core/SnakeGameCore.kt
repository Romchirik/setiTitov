package nsu.titov.core

import mu.KotlinLogging
import nsu.titov.core.data.CoreConfig
import nsu.titov.core.data.PlayerWrapper
import nsu.titov.core.data.Playfield
import nsu.titov.core.data.Point
import nsu.titov.proto.SnakeProto
import nsu.titov.utils.coordToPoint
import nsu.titov.utils.invertDir
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class SnakeGameCore(private val config: CoreConfig) : GameCore {
    private val logger = KotlinLogging.logger {}

    //entities
    private val snakes: MutableMap<Int, Snake> = ConcurrentHashMap()
    private var players: MutableMap<Int, PlayerWrapper> = ConcurrentHashMap()
    private val foods: MutableList<Point> = ArrayList()
    private val playfield: Playfield = Playfield(config.width, config.height)

    private var targetFoodCount: Int = (config.foodStatic + players.size * config.foodPerPlayer).toInt()

    private var randomNumberGenerator: Random = Random(System.nanoTime())

    /**
     *  Game cycle:
     *  1. Move snakes
     *  2. Check collisions
     *  3. Generate new foods
     *  4. Return new result
     */
    override fun tick() {
//        logger.debug { "Updating game state" }
        targetFoodCount = (config.foodStatic + players.size * config.foodPerPlayer).toInt()

        players.forEach { (id, player) -> snakes[id]?.direction = player.lastTurn }
        snakes.forEach { (_, snake) -> snake.tick() }

        val removeIds: MutableSet<Int> = HashSet()
        snakes.forEach { (id0, snake0) ->
            snakes.forEach { (id1, snake1) ->
                if (id0 != id1 && snake0.ifCollide(snake1.getHead())) {
                    removeIds.add(id1)
                }
            }
            if (snake0.selfCollide()) {
                removeIds.add(id0)
            }
        }

        removeIds.forEach { id ->
            snakes.remove(id)
            players.remove(id)
        }

        val eatenFoods: MutableSet<Point> = HashSet()
        snakes.forEach { (id, snake) ->
            foods.forEach { food ->
                if (snake.ifCollide(food)) {
                    if (eatenFoods.add(food)) {
                        snake.grow()
                        players[id]!!.score++
                    }
                }
            }
        }
        eatenFoods.forEach { food -> foods.remove(food) }

        if (foods.size < targetFoodCount) {
            for (i in 0 until targetFoodCount - foods.size) {
                foods.add(generateFood())
            }
        }

        players = players.filter { (id, _) -> snakes.containsKey(id)}.toMutableMap()
    }

    private fun generateFood(): Point {
        while (true) {
            val x = randomNumberGenerator.nextInt()
            val y = randomNumberGenerator.nextInt()
            val point = Point(x, y)
            playfield.normalizeDirty(point)

            if (!checkCollisions(point)) return point
        }
    }

    private fun checkCollisions(point: Point): Boolean {
        for (snake in snakes) {
            if (snake.value.ifCollide(point)) {
                return true
            }
        }
        if (point in foods) return true
        return false
    }

    private fun checkFree(point: Point): Boolean {
        return true
    }

    override fun putTurn(id: Int, dir: SnakeProto.Direction) {
        players[id]?.let { wrapper -> wrapper.lastTurn = if (dir != invertDir(wrapper.lastTurn)) dir else return }
            ?: logger.error { "Player with id: $id not found, unable put next turn" }
    }

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
        snakes[player.id] = Snake(Point(0, 1), Point(0, -1), playfield)
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

    companion object {
        fun fromProtoState(state: SnakeProto.GameState): SnakeGameCore {

            val tmp = SnakeGameCore(
                CoreConfig(
                    width = state.config.width,
                    height = state.config.height,
                    foodStatic = state.config.foodStatic,
                    foodPerPlayer = state.config.foodPerPlayer,
                    deadFoodProbe = state.config.deadFoodProb
                )
            )

            //restoring foods
            state.foodsList.forEach { food ->
                tmp.foods.add(coordToPoint(food))
            }

            //restoring snakes
            val playfield = Playfield(state.config.width, state.config.height)
            state.snakesList.forEach { snake ->
                val mySnake = Snake(snake.pointsList.map { coord -> coordToPoint(coord) }, playfield)
                mySnake.direction = snake.headDirection
                if (SnakeProto.GameState.Snake.SnakeState.ZOMBIE == snake.state) {
                    mySnake.setStateZombie()
                }
                tmp.snakes[snake.playerId] = mySnake
            }

            //restoring players
            state.players.playersList.forEach { player ->
                tmp.players[player.id] = PlayerWrapper(
                    id = player.id,
                    playerType = player.type,
                    lastTurn = tmp.snakes[player.id]!!.direction,
                    score = player.score
                )
            }


            return tmp
        }
    }
}