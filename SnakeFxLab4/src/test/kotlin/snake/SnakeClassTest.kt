package snake

import nsu.titov.core.Snake
import nsu.titov.core.data.Playfield
import nsu.titov.core.data.Point
import nsu.titov.proto.SnakeProto
import org.junit.Test
import kotlin.test.assertEquals

class SnakeClassTest {
    @Test
    fun snakeBuildTest() {
        val field = Playfield(6, 5)

        val testList0 = listOf(Point(1, 1), Point(1, 0))
        val testList1 = listOf(Point(5, 1), Point(1, 0))
        val testList2 = listOf(Point(5, 1), Point(3, 0), Point(0, 2), Point(-3, 0))
        val testList3 = listOf(Point(0, 0), Point(5, 0))
        val testList4 = listOf(Point(3, 0), Point(0, -1))
        val testList5 = listOf(Point(0, 1), Point(-1, 0))

        val snake0 = Snake(testList0, field)
        val snake1 = Snake(testList1, field)
        val snake2 = Snake(testList2, field)
        val snake3 = Snake(testList3, field)
        val snake4 = Snake(testList4, field)
        val snake5 = Snake(testList5, field)

        //snake0
        assertEquals(snake0.getBody()[0], Point(1, 1))
        assertEquals(snake0.getBody()[1], Point(2, 1))
        assertEquals(snake0.direction, SnakeProto.Direction.LEFT)

        //snake1
        assertEquals(snake1.getBody()[0], Point(5, 1))
        assertEquals(snake1.getBody()[1], Point(0, 1))
        assertEquals(snake0.direction, SnakeProto.Direction.LEFT)

        //snake2
        assertEquals(snake2.getBody()[0], Point(5, 1))
        assertEquals(snake2.getBody()[1], Point(0, 1))
        assertEquals(snake2.getBody()[2], Point(1, 1))
        assertEquals(snake2.getBody()[3], Point(2, 1))
        assertEquals(snake2.getBody()[4], Point(2, 2))
        assertEquals(snake2.getBody()[5], Point(2, 3))
        assertEquals(snake2.getBody()[6], Point(1, 3))
        assertEquals(snake2.getBody()[7], Point(0, 3))
        assertEquals(snake2.getBody()[8], Point(5, 3))
        assertEquals(snake0.direction, SnakeProto.Direction.LEFT)

        //snake3
        assertEquals(snake3.getBody()[0], Point(0, 0))
        assertEquals(snake3.getBody()[1], Point(1, 0))
        assertEquals(snake3.getBody()[2], Point(2, 0))
        assertEquals(snake3.getBody()[3], Point(3, 0))
        assertEquals(snake3.getBody()[4], Point(4, 0))
        assertEquals(snake3.getBody()[5], Point(5, 0))
        assertEquals(snake0.direction, SnakeProto.Direction.LEFT)

        //snake4
        assertEquals(snake4.getBody()[0], Point(3, 0))
        assertEquals(snake4.getBody()[1], Point(3, 4))
        assertEquals(snake0.direction, SnakeProto.Direction.LEFT)

        //snake5
        assertEquals(snake5.getBody()[0], Point(0, 1))
        assertEquals(snake5.getBody()[1], Point(5, 1))
        assertEquals(snake5.direction, SnakeProto.Direction.RIGHT)

        val ser0 = snake0.serialize()
        val ser1 = snake1.serialize()
        val ser2 = snake2.serialize()
        val ser3 = snake3.serialize()
        val ser4 = snake4.serialize()
        val ser5 = snake5.serialize()

        assertEquals(testList0, ser0)
        assertEquals(testList1, ser1)
        assertEquals(testList2, ser2)
        assertEquals(testList3, ser3)
        assertEquals(testList4, ser4)
        assertEquals(testList5, ser5)
    }

    @Test
    fun growTest() {
        val field = Playfield(6, 5)
        val testList = listOf(Point(1, 1), Point(1, 0))
        val snake = Snake(testList, field)

        snake.grow()
        snake.grow()
        snake.grow()
        snake.grow()

        assertEquals(
            listOf(
                Point(1, 1),
                Point(2, 1),
                Point(3, 1),
                Point(4, 1),
                Point(5, 1),
                Point(0, 1)
            ),
            snake.getBody()
        )
    }

    @Test
    fun moveTest() {
        val field = Playfield(6, 5)
        val testList = listOf(Point(1, 1), Point(1, 0))
        val snake = Snake(testList, field)

        snake.tick()
        assertEquals(listOf(Point(0, 1), Point(1, 1)), snake.getBody())
        snake.tick()
        assertEquals(listOf(Point(5, 1), Point(0, 1)), snake.getBody())
        snake.tick()
        assertEquals(listOf(Point(4, 1), Point(5, 1)), snake.getBody())
        snake.tick()
        assertEquals(listOf(Point(3, 1), Point(4, 1)), snake.getBody())
        snake.tick()
        assertEquals(listOf(Point(2, 1), Point(3, 1)), snake.getBody())
        snake.tick()
        assertEquals(listOf(Point(1, 1), Point(2, 1)), snake.getBody())
    }
}