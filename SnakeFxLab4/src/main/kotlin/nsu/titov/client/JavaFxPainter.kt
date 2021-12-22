package nsu.titov.client

import javafx.application.Platform
import javafx.scene.paint.Color
import mu.KotlinLogging
import nsu.titov.event.Subscriber
import nsu.titov.net.Message
import nsu.titov.proto.SnakeProto
import nsu.titov.utils.coordToPoint
import nsu.titov.utils.dirToPoint
import nsu.titov.utils.pointToDir
import kotlin.math.abs

class JavaFxPainter(bundle: Any) : Painter, Subscriber {
    private val logger = KotlinLogging.logger {}
    private val bundle: Bundle

    init {
        this.bundle = bundle as Bundle
    }

    private fun paintFood(food: SnakeProto.GameState.Coord) {
        val context = bundle.canvas.graphicsContext2D
        context.fill = Color.YELLOW
        context.fillOval((CELL_SIZE * food.x).toDouble() + 4, (CELL_SIZE * food.y).toDouble() + 4, 12.0, 12.0)
    }

    private fun paintSnake(snake: SnakeProto.GameState.Snake, color: Color) {
        val context = bundle.canvas.graphicsContext2D
        context.fill = color

        val points = snake.pointsList

        var startPoint = coordToPoint(points[0])
        for (i in 1 until points.size) {
            val currOffset = coordToPoint(points[i])
            val modOffset = if (currOffset.x == 0) abs(currOffset.y) else abs(currOffset.x)
            val dirVec = dirToPoint(pointToDir(currOffset)) //to get unit vector

            for (i in 0 until modOffset + 1) {
                context.fillRect(
                    (startPoint.x * CELL_SIZE).toDouble(),
                    (startPoint.y * CELL_SIZE).toDouble(),
                    CELL_SIZE.toDouble(),
                    CELL_SIZE.toDouble()
                )
                startPoint += dirVec
            }
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
        logger.info { "field repaint started" }

        // 1. Paint grid
        paintGrid(state.state.config.width, state.state.config.height)

        // 2. Paint foods
        state.state.foodsList.forEach { food ->
            paintFood(food)
        }
        // 3. Paint snakes
        state.state.snakesList.forEach { snake ->
            paintSnake(snake, Color.BLACK)
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

    private fun paintConfig(config: SnakeProto.GameConfig) {
        bundle.foodRuleLabel.text = "Food rule: ${config.foodStatic}+${config.deadFoodProb}*x"
        bundle.fieldSizeLabel.text = "Field size: ${config.width}x${config.height}"
    }

    private fun paintPlayer(player: SnakeProto.GamePlayer) {

        bundle.currentGameInfoList.add("${player.score} ${player.name}")
    }

    override fun addAvailableSever(server: SnakeProto.GameMessage.AnnouncementMsg) {
        logger.info { "new server appeared" }
        // TODO: 08.12.2021 add server info painting
    }

    @Synchronized
    override fun update(msg: Message) {
        val message = msg.msg
        when (message.typeCase) {
            SnakeProto.GameMessage.TypeCase.STATE ->
                Platform.runLater { repaint(message.state) }
            SnakeProto.GameMessage.TypeCase.ANNOUNCEMENT ->
                Platform.runLater { addAvailableSever(message.announcement) }
            SnakeProto.GameMessage.TypeCase.ERROR -> {
                Platform.runLater { addErrorMessage(message.error) }
            }
            else -> logger.error {
                "Painter received unacceptable message type: ${message.typeCase}"
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