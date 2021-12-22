package nsu.titov.client

import javafx.collections.FXCollections
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.canvas.Canvas
import javafx.scene.control.Label
import javafx.scene.control.ListView
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import mu.KotlinLogging
import nsu.titov.net.NetWorker
import nsu.titov.net.SocketEndpoint
import nsu.titov.net.ThreadNetWorker
import nsu.titov.proto.SnakeProto.*
import nsu.titov.server.ServerConfig
import nsu.titov.server.SnakeServerUtils
import nsu.titov.settings.SettingsProvider
import nsu.titov.utils.MessageIdProvider
import java.net.InetAddress
import java.net.URL
import java.util.*


class SnakeFX : Initializable {
    private val logger = KotlinLogging.logger {}


    @FXML
    lateinit var canvas: Canvas

    @FXML
    lateinit var hostNameLabel: Label

    @FXML
    lateinit var fieldSizeLabel: Label

    @FXML
    lateinit var foodRuleLabel: Label

    @FXML
    lateinit var errorLabel: Label

    @FXML
    lateinit var currentGameInfo: ListView<String>
    private val currentGameInfoList = FXCollections.observableArrayList<String>()

    @FXML
    lateinit var availableServers: ListView<AnnounceItem>
    private val availableServersList = FXCollections.observableArrayList<AnnounceItem>()


    //TODO(move to global state)
    private val role = NodeRole.VIEWER
    private val serverAddress = InetAddress.getLocalHost()
    private val serverPort = 6734

    private val netWorker: NetWorker = ThreadNetWorker(SocketEndpoint(InetAddress.getLocalHost(), 6736))
    private val netWorkerThread: Thread = Thread(netWorker)


    fun handleKeyboard(keyEvent: KeyEvent) {
        logger.debug { "Handle keyboard triggered, key: ${keyEvent.code.char}" }
        val action: Direction = when (keyEvent.code) {
            KeyCode.W -> Direction.UP
            KeyCode.A -> Direction.LEFT
            KeyCode.S -> Direction.DOWN
            KeyCode.D -> Direction.RIGHT
            else -> return
        }

        when (role) {
            NodeRole.VIEWER -> return
            else -> {
                val steer: GameMessage.SteerMsg = GameMessage.SteerMsg
                    .newBuilder()
                    .setDirection(action)
                    .build()
                val message: GameMessage = GameMessage.newBuilder()
                    .setMsgSeq(MessageIdProvider.getNextMessageId())
                    .setSteer(steer)
                    .build()
            }

        }
    }

    fun handleExitGame() {
        logger.debug { "Exit game button triggered" }
    }

    fun handleStartNewGame() {
        logger.debug { "Start new game button triggered" }
        //TODO collect all data for starting and start server, now just stub data

        val stub = ServerConfig()
        SnakeServerUtils.startServer(stub)
    }

    fun handleJoinGame() {
        logger.debug { "Join game button triggered" }
//        val server = availableServers.selectionModel.selectedItem
//        if (availableServers.selectionModel.selectedItem == null) {
//            Platform.runLater { errorLabel.text = "Select server before joining game" }
//            return
//        }

        val joinMessage = GameMessage.newBuilder()
            .setJoin(
                GameMessage.JoinMsg.newBuilder().setName(SettingsProvider.settings?.playerName).build()
            )
            .setMsgSeq(MessageIdProvider.getNextMessageId())
            .build()

        netWorker.putMessage(joinMessage, serverAddress, serverPort)
    }

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        val painter: Painter = JavaFxPainter(
            Bundle(
                canvas = this.canvas,
                hostNameLabel = this.hostNameLabel,
                fieldSizeLabel = this.fieldSizeLabel,
                foodRuleLabel = this.foodRuleLabel,
                errorLabel = this.errorLabel,

                currentGameInfo = this.currentGameInfo,
                currentGameInfoList = this.currentGameInfoList,

                availableServers = this.availableServers,
                availableServersList = this.availableServersList
            )
        )

        currentGameInfo.items = currentGameInfoList
        currentGameInfo.isEditable = false
        availableServers.items = availableServersList
        availableServers.isEditable = false


        netWorker.subscribe(painter, GameMessage.TypeCase.STATE)
        netWorker.subscribe(painter, GameMessage.TypeCase.ANNOUNCEMENT)
        netWorker.subscribe(painter, GameMessage.TypeCase.ERROR)

        netWorkerThread.start()
    }
}