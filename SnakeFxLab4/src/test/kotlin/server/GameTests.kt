package server

import nsu.titov.core.Game
import nsu.titov.core.SnakeGame
import org.junit.Test

class GameTests {
    @Test
    fun testGameSimpleTest() {
        val tmp: Game = SnakeGame()
        tmp.tick()
    }
}