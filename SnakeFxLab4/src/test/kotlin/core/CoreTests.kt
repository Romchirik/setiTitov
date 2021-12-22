package core

import nsu.titov.core.GameCore
import nsu.titov.core.SnakeGameCore
import nsu.titov.core.data.PlayerWrapper
import nsu.titov.core.data.Point
import nsu.titov.proto.SnakeProto
import org.junit.Test
import kotlin.test.assertTrue

class CoreTests {
    @Test
    fun coreSimpleTest() {
        val core: GameCore = SnakeGameCore(Point(10, 10))

        assertTrue { core.getFoods().isEmpty() }
        assertTrue { core.getPlayers().isEmpty() }
        assertTrue { core.getSnakes().isEmpty() }

        core.addPlayer(
            PlayerWrapper(
                id = 1,
                lastTurn = SnakeProto.Direction.DOWN
            )
        )
    }
}