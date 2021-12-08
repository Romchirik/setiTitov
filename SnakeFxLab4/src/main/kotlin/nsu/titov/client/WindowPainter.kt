package nsu.titov.client

import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.paint.Color
import mu.KotlinLogging
import nsu.titov.proto.SnakeProto

class WindowPainter : GraphicsPainter {
    private val logger = KotlinLogging.logger {}

    override fun repaint(newState: SnakeProto.GameMessage.StateMsg, canvasCustom: Any) {
        val canvas = canvasCustom as Canvas
        val context = canvas.graphicsContext2D

        context.fill = Color.GRAY
        context.lineWidth = 0.2

        //horizontal lines
        for (i in 0 until (newState.state.config.height + 1)) {
            context.strokeLine(
                (i * CELL_SIZE).toDouble(), 0.0, (i * CELL_SIZE).toDouble(),
                (CELL_SIZE * newState.state.config.height).toDouble()
            )
        }

        //vertical lines
        for (i in 0 until (newState.state.config.width + 1)) {
            context.strokeLine(
                0.0, (i * CELL_SIZE).toDouble(), (CELL_SIZE * newState.state.config.width).toDouble(),
                (i * CELL_SIZE).toDouble()

            )
        }

        context.fill = Color.YELLOW
        newState.state.foodsList.forEach { food ->
            context.fillOval(
                (food.x * CELL_SIZE).toDouble(),
                (food.y * CELL_SIZE).toDouble(), CELL_SIZE.toDouble(), CELL_SIZE.toDouble()
            )
        }

        newState.state.snakesList.forEach { snake ->
            paintSnake(context, snake, Color.AQUAMARINE)
        }

    }

    private fun paintSnake(context: GraphicsContext, snake: SnakeProto.GameState.Snake, color: Color) {
        val prevColor = context.fill
        context.fill = color
        context.lineWidth = CELL_SIZE.toDouble()

        var prevOffsetX = snake.getPoints(0).x * CELL_SIZE + 10
        var prevOffsetY = snake.getPoints(0).y * CELL_SIZE + 10

        for (i in 1 until snake.pointsCount) {
            val currNode = snake.getPoints(i)

            context.strokeLine(
                prevOffsetX.toDouble(),
                prevOffsetY.toDouble(),
                (currNode.x * CELL_SIZE + prevOffsetX).toDouble(),
                (currNode.y * CELL_SIZE + prevOffsetY).toDouble()
            )

            prevOffsetX += currNode.x * CELL_SIZE
            prevOffsetY += currNode.y * CELL_SIZE
        }
        context.fill = prevColor
    }

    companion object {
        private const val CELL_SIZE = 20;
    }

}