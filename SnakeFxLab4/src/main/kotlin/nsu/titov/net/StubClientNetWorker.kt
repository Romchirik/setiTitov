package nsu.titov.net

import nsu.titov.proto.SnakeProto.*
import nsu.titov.proto.SnakeProto.GameMessage.StateMsg
import nsu.titov.proto.SnakeProto.GameState.Coord
import nsu.titov.proto.SnakeProto.GameState.Snake.SnakeState
import nsu.titov.utils.MessageIdProvider
import java.net.InetAddress


class StubClientNetWorker(private val delay: Long) : NetWorker(StubEndpoint(0)), Runnable {

    private val messages: ArrayDeque<GameMessage> = generateMessages()
    override fun putMessage(message: GameMessage, ip: InetAddress, port: Int) {
        TODO("Not yet implemented")
    }


    override fun stop() {
        TODO("Not yet implemented")
    }

    override fun setEndpoint(endpoint: ConnectionEndpoint): NetWorker {
        TODO("Not yet implemented")
    }

    override fun run() {
        return
    }

    private fun generateMessages(): ArrayDeque<GameMessage> {
        val tmp = ArrayDeque<GameMessage>()

        val snake1: GameState.Snake = GameState.Snake.newBuilder()
            .setPlayerId(1)
            .setHeadDirection(Direction.UP)
            .setState(SnakeState.ALIVE)
            .addPoints(coord(5, 1)) // голова
            .addPoints(coord(0, 1))
            .build()
        val snake2: GameState.Snake = GameState.Snake.newBuilder()
            .setPlayerId(1)
            .setHeadDirection(Direction.LEFT)
            .setState(SnakeState.ALIVE)
            .addPoints(coord(4, 1)) // голова
            .addPoints(coord(1, 0))
            .build()
        val snake3: GameState.Snake = GameState.Snake.newBuilder()
            .setPlayerId(1)
            .setHeadDirection(Direction.LEFT)
            .setState(SnakeState.ALIVE)
            .addPoints(coord(3, 1)) // голова
            .addPoints(coord(1, 0))
            .build()
        val snake4: GameState.Snake = GameState.Snake.newBuilder()
            .setPlayerId(1)
            .setHeadDirection(Direction.DOWN)
            .setState(SnakeState.ALIVE)
            .addPoints(coord(3, 2)) // голова
            .addPoints(coord(0, -1))
            .build()
        val snake5: GameState.Snake = GameState.Snake.newBuilder()
            .setPlayerId(1)
            .setHeadDirection(Direction.RIGHT)
            .setState(SnakeState.ALIVE)
            .addPoints(coord(4, 2)) // голова
            .addPoints(coord(-1, 0))
            .build()
        val snake6: GameState.Snake = GameState.Snake.newBuilder()
            .setPlayerId(1)
            .setHeadDirection(Direction.RIGHT)
            .setState(SnakeState.ALIVE)
            .addPoints(coord(5, 2)) // голова
            .addPoints(coord(-1, 0))
            .build()

        val config = GameConfig.newBuilder()
            .setWidth(10)
            .setHeight(10) // Все остальные параметры имеют значения по умолчанию
            .build()
        val snake: GameState.Snake = GameState.Snake.newBuilder()
            .setPlayerId(1)
            .setHeadDirection(Direction.LEFT)
            .setState(SnakeState.ALIVE)
            .addPoints(coord(5, 1)) // голова
            .addPoints(coord(1, 0))
            .build()
        val playerBob = GamePlayer.newBuilder()
            .setId(1)
            .setRole(NodeRole.MASTER)
            .setIpAddress("") // MASTER не отправляет собственный IP
            .setPort(20101)
            .setName("Bob")
            .setScore(8)
            .build()
        val players = GamePlayers.newBuilder()
            .addPlayers(playerBob)
            .build()

        val state1: GameState = GameState.newBuilder()
            .setStateOrder(193)
            .addSnakes(snake1)
            .setPlayers(players)
            .setConfig(config)
            .addFoods(coord(7, 6))
            .addFoods(coord(8, 7))
            .build()
        val state2: GameState = GameState.newBuilder()
            .setStateOrder(193)
            .addSnakes(snake2)
            .setPlayers(players)
            .setConfig(config)
            .addFoods(coord(7, 6))
            .addFoods(coord(8, 7))
            .build()
        val state3: GameState = GameState.newBuilder()
            .setStateOrder(193)
            .addSnakes(snake3)
            .setPlayers(players)
            .setConfig(config)
            .addFoods(coord(7, 6))
            .addFoods(coord(8, 7))
            .build()
        val state4: GameState = GameState.newBuilder()
            .setStateOrder(193)
            .addSnakes(snake4)
            .setPlayers(players)
            .setConfig(config)
            .addFoods(coord(7, 6))
            .addFoods(coord(8, 7))
            .build()
        val state5: GameState = GameState.newBuilder()
            .setStateOrder(193)
            .addSnakes(snake5)
            .setPlayers(players)
            .setConfig(config)
            .addFoods(coord(7, 6))
            .addFoods(coord(8, 7))
            .build()
        val state6: GameState = GameState.newBuilder()
            .setStateOrder(193)
            .addSnakes(snake6)
            .setPlayers(players)
            .setConfig(config)
            .addFoods(coord(7, 6))
            .addFoods(coord(8, 7))
            .build()

        val stateMsg1 = StateMsg.newBuilder()
            .setState(state1)
            .build()

        val stateMsg2 = StateMsg.newBuilder()
            .setState(state2)
            .build()

        val stateMsg3 = StateMsg.newBuilder()
            .setState(state3)
            .build()

        val stateMsg4 = StateMsg.newBuilder()
            .setState(state4)
            .build()

        val stateMsg5 = StateMsg.newBuilder()
            .setState(state5)
            .build()

        val stateMsg6 = StateMsg.newBuilder()
            .setState(state6)
            .build()


        var gameMessage1 = GameMessage.newBuilder()
            .setMsgSeq(MessageIdProvider.getNextMessageId())
            .setState(stateMsg1)
            .build()

        var gameMessage2 = GameMessage.newBuilder()
            .setMsgSeq(MessageIdProvider.getNextMessageId())
            .setState(stateMsg2)
            .build()

        var gameMessage3 = GameMessage.newBuilder()
            .setMsgSeq(MessageIdProvider.getNextMessageId())
            .setState(stateMsg3)
            .build()

        var gameMessage4 = GameMessage.newBuilder()
            .setMsgSeq(MessageIdProvider.getNextMessageId())
            .setState(stateMsg4)
            .build()

        var gameMessage5 = GameMessage.newBuilder()
            .setMsgSeq(MessageIdProvider.getNextMessageId())
            .setState(stateMsg5)
            .build()

        var gameMessage6 = GameMessage.newBuilder()
            .setMsgSeq(MessageIdProvider.getNextMessageId())
            .setState(stateMsg6)
            .build()

        val gameMessage7 = GameMessage.newBuilder()
            .setMsgSeq(MessageIdProvider.getNextMessageId())
            .setError(GameMessage.ErrorMsg.newBuilder().setErrorMessage("Some bullshit occurred").build())
            .build()
        tmp.addLast(gameMessage1)
        tmp.addLast(gameMessage2)
        tmp.addLast(gameMessage3)
        tmp.addLast(gameMessage4)
        tmp.addLast(gameMessage5)
        tmp.addLast(gameMessage6)
        tmp.addLast(gameMessage7)
        return tmp
    }


    private fun coord(x: Int, y: Int): Coord? {
        return Coord.newBuilder().setX(x).setY(y).build()
    }



}