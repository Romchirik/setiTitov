package nsu.titov.client

import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.canvas.Canvas
import javafx.scene.control.Label
import javafx.scene.control.ListView
import javafx.scene.control.SelectionMode
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import nsu.titov.net.ClientNetWorker
import nsu.titov.proto.SnakeProto.*
import nsu.titov.proto.SnakeProto.GameMessage.StateMsg
import nsu.titov.proto.SnakeProto.GameState.Coord
import nsu.titov.proto.SnakeProto.GameState.Snake
import nsu.titov.proto.SnakeProto.GameState.Snake.SnakeState
import nsu.titov.utils.MessageIdProvider
import java.net.URL
import java.util.*


class SnakeFX : Initializable, Controller {


    @FXML
    lateinit var errorMessagesLabel: Label

    @FXML
    lateinit var gameFieldCanvas: Canvas

    @FXML
    lateinit var availableServers: ListView<AnnounceItem>
    var availableServersList: ObservableList<AnnounceItem> = FXCollections.observableArrayList()


    //TODO(move to global config)
    val role = NodeRole.MASTER
    private lateinit var announcerThread: Thread
    private lateinit var netWorkerThread: Thread

    //private val announcer = Announcer
    private val netWorker = ClientNetWorker()

    private val painter: GraphicsPainter = WindowPainter()

    private fun setLabelText(label: Label, text: String) {
        Platform.runLater { label.text = text }
    }


    //User actions handlers
    fun handleKeyboard(keyEvent: KeyEvent) {
        val action: Direction = when (keyEvent.code) {
            KeyCode.W -> Direction.UP
            KeyCode.A -> Direction.LEFT
            KeyCode.S -> Direction.DOWN
            KeyCode.D -> Direction.RIGHT
            else -> return
        }

        when (role) {
            NodeRole.MASTER, NodeRole.DEPUTY, NodeRole.NORMAL -> {
                val steer: GameMessage.SteerMsg = GameMessage.SteerMsg
                    .newBuilder()
                    .setDirection(action)
                    .build()
                val message: GameMessage = GameMessage.newBuilder()
                    .setMsgSeq(MessageIdProvider.getNextMessageId())
                    .setSteer(steer)
                    .build()
                netWorker.putMessage(message)
            }
            NodeRole.VIEWER -> {//ignoring
            }
        }
    }


    fun handleStartNewGame() {


    }


    @Synchronized
    override fun setErrorMessage(message: String) {
        errorMessagesLabel.text = message
    }


    /** NetWorker hook, work as wrapper to prevent handling messages
     * from netWorkerThread. Uses Platform.runLater tio run code in JavaFX thread
     *
     * @param message incoming message to handle
     */
    @Synchronized
    override fun handleMessage(message: GameMessage) {
        Platform.runLater {handleMessageNotSynced(message)}
    }

    private fun handleMessageNotSynced(message: GameMessage) {

    }

    fun handleExitGame() {
        TODO("Not yet implemented")
    }

    fun handleJoinGame() {
        val selectedServer = availableServers.selectionModel.selectedItem
        if (selectedServer == null) {
            setLabelText(errorMessagesLabel, "Select server before connecting")
            return
        }

        val message = GameMessage.newBuilder()
            .setMsgSeq(MessageIdProvider.getNextMessageId())
            .setJoin(
                GameMessage.JoinMsg.newBuilder()
                    .setName("Big Flopper")
                    .build()
            ).build()
        netWorker.putMessage(message)
    }

    private fun stub(): StateMsg {
        val config = GameConfig.newBuilder()
            .setWidth(10)
            .setHeight(10) // Все остальные параметры имеют значения по умолчанию
            .build()
        val snake: Snake = Snake.newBuilder()
            .setPlayerId(1)
            .setHeadDirection(Direction.LEFT)
            .setState(SnakeState.ALIVE)
            .addPoints(cord(5, 1)) // голова
            .addPoints(cord(3, 0))
            .addPoints(cord(0, 2))
            .addPoints(cord(-4, 0))
            .build()
        // Единственный игрок в игре, он же MASTER
        // Единственный игрок в игре, он же MASTER
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
        val state: GameState = GameState.newBuilder()
            .setStateOrder(193)
            .addSnakes(snake)
            .setPlayers(players)
            .setConfig(config)
            .addFoods(cord(7, 6))
            .addFoods(cord(8, 7))
            .build()
        return StateMsg.newBuilder().setState(state).build()
    }

    private fun cord(x: Int, y: Int): Coord? {
        return Coord.newBuilder().setX(x).setY(y).build()
    }

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        netWorkerThread = Thread(netWorker)
        netWorkerThread.start()
        painter.repaint(stub(), gameFieldCanvas)

        availableServers.items = availableServersList
        availableServers.selectionModel.selectionMode = SelectionMode.SINGLE;
    }
}