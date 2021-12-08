package snake

import nsu.titov.core.FieldConfig
import nsu.titov.core.Point
import nsu.titov.core.Snake
import nsu.titov.proto.SnakeProto
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

class SnakeClassTest {
    @Test
    fun snakeBuildTest() {
        val message: SnakeProto.GameState.Snake = SnakeProto.GameState.Snake.newBuilder()
            .setPlayerId(1)
            .setHeadDirection(SnakeProto.Direction.LEFT)
            .setState(SnakeProto.GameState.Snake.SnakeState.ALIVE)
            .addPoints(cord(5, 1)) // голова
            .addPoints(cord(3, 0))
            .addPoints(cord(0, 2))
            .addPoints(cord(-4, 0))
            .build()
        val start = Point(0, 0)
        val offsetInvalid = Point(3, 4)
        val snake1 = Snake(message)

        assertFails { Snake(start, offsetInvalid) }
        assertEquals(snake1.getSize(), 10);

        snake1.tick()

    }

    @Test
    fun serializationTest() {
        val message: SnakeProto.GameState.Snake = SnakeProto.GameState.Snake.newBuilder()
            .setPlayerId(1)
            .setHeadDirection(SnakeProto.Direction.LEFT)
            .setState(SnakeProto.GameState.Snake.SnakeState.ALIVE)
            .addPoints(cord(5, 1)) // голова
            .addPoints(cord(3, 0))
            .addPoints(cord(0, 2))
            .addPoints(cord(-4, 0))
            .build()
        val snake = Snake(message)
        val newMessage = snake.toProto(FieldConfig(10, 10))
        val a = newMessage.setState(SnakeProto.GameState.Snake.SnakeState.ALIVE).setPlayerId(1).build()

        assertEquals(message, a)

    }

    private fun cord(x: Int, y: Int): SnakeProto.GameState.Coord? {
        return SnakeProto.GameState.Coord.newBuilder().setX(x).setY(y).build()
    }
}