package nsu.titov.client

import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.canvas.Canvas
import javafx.scene.control.Label
import javafx.scene.control.ListView
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import mu.KotlinLogging
import nsu.titov.event.Subscriber
import nsu.titov.net.Message
import nsu.titov.net.NetWorker
import nsu.titov.net.SocketEndpoint
import nsu.titov.net.client.ClientThreadNetWorker
import nsu.titov.proto.SnakeProto.*
import nsu.titov.server.ServerConfig
import nsu.titov.server.SnakeServerUtils
import nsu.titov.settings.SettingsProvider
import nsu.titov.utils.MessageIdProvider
import java.net.InetAddress
import java.net.URL
import java.util.*
import kotlin.concurrent.fixedRateTimer


class SnakeFX : Initializable, Subscriber {
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

    private val availableServersBuffer = ArrayList<AnnounceItem>()
    private val clearTimer = fixedRateTimer(
        name = "Core tick timer",
        daemon = true,
        initialDelay = 0L,
        period = SettingsProvider.getSettings().announceDelayMs.toLong()
    ) { fireAnnounceUpdate() }


    private val netWorker: NetWorker = ClientThreadNetWorker(SocketEndpoint(0))
    private val netWorkerThread: Thread = Thread(netWorker, "Client net worker")

    private val announcer: AnnounceHandler = AnnounceHandler()
    private val announcerThread: Thread = Thread(announcer, "Announcer thread")

    private lateinit var painter: Painter

    private fun fireAnnounceUpdate() {

        Platform.runLater {
            var selectedItem: AnnounceItem? = null
            if (null != availableServers.selectionModel.selectedItem) {
                selectedItem = availableServers.selectionModel.selectedItem
            }
            availableServersList.clear()
            availableServersList.addAll(availableServersBuffer)
            availableServersBuffer.clear()

            if (selectedItem != null) {
                val tmp =
                    availableServersList.find { announceItem ->
                        announceItem.ip == selectedItem.ip && announceItem.port == selectedItem.port
                    }
                if (null != tmp){
                    if(tmp.canJoin) {
                        availableServers.selectionModel.select(tmp)
                    }
                }
            }
        }
    }

    fun handleKeyboard(keyEvent: KeyEvent) {
        if (StateProvider.getState().id == 0) {
            logger.warn { "Client not connected, unable to handle steer" }
            return
        }
        val action: Direction = when (keyEvent.code) {
            KeyCode.W -> Direction.UP
            KeyCode.A -> Direction.LEFT
            KeyCode.S -> Direction.DOWN
            KeyCode.D -> Direction.RIGHT
            else -> return
        }

        when (StateProvider.getState().role) {
            NodeRole.VIEWER -> return
            else -> {
                val steer: GameMessage.SteerMsg = GameMessage.SteerMsg
                    .newBuilder()
                    .setDirection(action)
                    .build()
                val message: GameMessage = GameMessage.newBuilder()
                    .setMsgSeq(MessageIdProvider.getNextMessageId())
                    .setSenderId(StateProvider.getState().id)
                    .setSteer(steer)
                    .build()

                netWorker.putMessage(
                    message,
                    StateProvider.getState().serverAddress,
                    StateProvider.getState().serverPort
                )
            }
        }
    }

    fun handleExitGame() {
        if (0 == StateProvider.getState().id) {
            return
        }
    }

    fun handleStartNewGame() {
        if (SnakeServerUtils.isRunning()) {
            return
        }
        val settings = SettingsProvider.getSettings()
        val notStub = ServerConfig(
            stateTickDelayMs = settings.stateTickDelayMs,
            pingDelayMs = settings.pingDelayMs,
            timeoutDelayMs = settings.timeoutDelayMs,
            playfieldHeight = settings.playfieldHeight,
            playfieldWidth = settings.playfieldWidth
        )

        SnakeServerUtils.startServer(notStub)
        joinGame(
            AnnounceItem(
                playersCount = 0,
                canJoin = true,
                ip = InetAddress.getLocalHost(),
                port = SnakeServerUtils.getPort()
            )
        )

    }

    private fun joinGame(announceItem: AnnounceItem) {
        val joinMessage = GameMessage.newBuilder()
            .setJoin(
                GameMessage.JoinMsg.newBuilder().setName(SettingsProvider.getSettings().playerName).build()
            )
            .setMsgSeq(MessageIdProvider.getNextMessageId())
            .build()

        netWorker.putMessage(joinMessage, announceItem.ip, announceItem.port)
    }

    fun handleJoinGame() {
        if (StateProvider.getState().id != 0) {
            logger.warn { "Already joined game" }
            return
        }
        if (null != availableServers.selectionModel.selectedItem) {
            joinGame(availableServers.selectionModel.selectedItem)
        }
    }

    override fun update(message: Message) {
        when (message.msg.typeCase) {
            GameMessage.TypeCase.ACK -> handleAck(message)
            GameMessage.TypeCase.ROLE_CHANGE -> handleRoleChange(message.msg)
            GameMessage.TypeCase.ANNOUNCEMENT -> handleAnnounce(message)
            else -> return
        }
    }

    private fun handleAnnounce(msg: Message) {
        availableServersBuffer.add(AnnounceItem.fromProto(msg))
    }

    private fun handleRoleChange(message: GameMessage) {
        StateProvider.getState().role = message.roleChange.receiverRole
        if (message.roleChange.receiverRole == NodeRole.VIEWER) {
            StateProvider.getState().id = 0
        }
    }

    private fun handleAck(message: Message) {
        StateProvider.getState().serverAddress = message.ip
        StateProvider.getState().serverPort = message.port
        StateProvider.getState().id = message.msg.receiverId
        netWorker.unsubscribe(this, GameMessage.TypeCase.ACK)
    }

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        painter = JavaFxPainter(
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

        netWorker.subscribe(this, GameMessage.TypeCase.ACK)
        netWorker.subscribe(this, GameMessage.TypeCase.ROLE_CHANGE)
        netWorker.subscribe(painter, GameMessage.TypeCase.STATE)
        netWorker.subscribe(painter, GameMessage.TypeCase.ERROR)

        announcer.subscribe(this, GameMessage.TypeCase.ANNOUNCEMENT)

        netWorkerThread.start()
        announcerThread.start()
    }
}