package nsu.titov.client

import javafx.application.Platform
import javafx.scene.paint.Color
import mu.KotlinLogging
import nsu.titov.core.Snake
import nsu.titov.core.data.Playfield
import nsu.titov.event.Subscriber
import nsu.titov.net.Message
import nsu.titov.proto.SnakeProto
import nsu.titov.utils.coordToPoint

class JavaFxPainter(bundle: Any) : Painter, Subscriber {
    private val logger = KotlinLogging.logger {}
    private val bundle: Bundle

    init {
        this.bundle = bundle as Bundle
    }

    private fun paintFood(food: SnakeProto.GameState.Coord) {
        val context = bundle.canvas.graphicsContext2D
        context.fill = Color.GREEN
        context.fillOval((CELL_SIZE * food.x).toDouble() + 4, (CELL_SIZE * food.y).toDouble() + 4, 12.0, 12.0)
    }

    private fun paintSnake(snake: Snake, color: Color) {
        val context = bundle.canvas.graphicsContext2D

        context.fill = color

        snake.getBody().forEach { point ->
            context.fillRect(
                (point.x * CELL_SIZE).toDouble(),
                (point.y * CELL_SIZE).toDouble(),
                CELL_SIZE.toDouble(),
                CELL_SIZE.toDouble()
            )
        }
    }

    private fun paintGrid(width: Int, height: Int) {
        val context = bundle.canvas.graphicsContext2D

        context.clearRect(0.0, 0.0, bundle.canvas.width, bundle.canvas.height)
        context.fill = Color.GRAY
        context.lineWidth = 0.5

        for (i in 0 until (width + 1)) {
            context.strokeLine(
                (CELL_SIZE * i).toDouble(),
                0.0,
                (CELL_SIZE * i).toDouble(),
                (height * CELL_SIZE).toDouble()
            )
        }
        for (i in 0 until (height + 1)) {
            context.strokeLine(
                0.0,
                (CELL_SIZE * i).toDouble(),
                (width * CELL_SIZE).toDouble(),
                (CELL_SIZE * i).toDouble()
            )
        }
    }

    override fun repaint(state: SnakeProto.GameMessage.StateMsg) {
        // 1. Paint grid
        paintGrid(state.state.config.width, state.state.config.height)

        // 2. Paint foods
        state.state.foodsList.forEach { food ->
            paintFood(food)
        }
        // 3. Paint snakes
        state.state.snakesList.forEach { snake ->
            val tmp = snake.pointsList.map { coord -> coordToPoint(coord) }
            val snek = Snake(tmp, Playfield(state.state.config.width, state.state.config.height))
            val color = if (snake.playerId == StateProvider.getState().id) Color.RED else Color.BLACK
            paintSnake(snek, color)
        }

        // 4. Paint players
        bundle.currentGameInfoList.clear()
        state.state.players.playersList.forEach { player ->
            if (player.role == SnakeProto.NodeRole.MASTER) {
                // 5. Paint config
                bundle.hostNameLabel.text = "Host name: ${player.name}"
            }
            paintPlayer(player)
        }

        // 5. Paint config
        paintConfig(state.state.config)

    }

    override fun repaintAvailableServers(aboba: List<AnnounceItem>) {
        bundle.availableServersList.clear()
        bundle.availableServersList.addAll(aboba)
    }

    private fun paintConfig(config: SnakeProto.GameConfig) {
        bundle.foodRuleLabel.text = "Food rule: ${config.foodStatic}+${config.deadFoodProb}*x"
        bundle.fieldSizeLabel.text = "Field size: ${config.width}x${config.height}"
    }

    private fun paintPlayer(player: SnakeProto.GamePlayer) {
        bundle.currentGameInfoList.add("${player.score} ${player.name}")
    }

    @Synchronized
    override fun update(message: Message) {
        val msg = message.msg
        when (msg.typeCase) {
            SnakeProto.GameMessage.TypeCase.STATE ->
                Platform.runLater { repaint(msg.state) }
            SnakeProto.GameMessage.TypeCase.ERROR -> {
                Platform.runLater { addErrorMessage(msg.error) }
            }
            else -> logger.error {
                "Painter received unacceptable message type: ${msg.typeCase}"
            }
        }
    }

    private fun addErrorMessage(error: SnakeProto.GameMessage.ErrorMsg) {
        bundle.errorLabel.text = error.errorMessage
    }

    companion object {
        private const val CELL_SIZE = 20
    }

}