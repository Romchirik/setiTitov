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
import nsu.titov.net.client.ClientThreadNetWorker
import nsu.titov.proto.SnakeProto.*
import nsu.titov.server.ServerConfig
import nsu.titov.server.SnakeServerUtils
import nsu.titov.settings.SettingsProvider
import nsu.titov.utils.MessageIdProvider
import nsu.titov.utils.ThreadManager
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


    private val netWorker: NetWorker = ClientThreadNetWorker()
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
                if (null != tmp) {
                    if (tmp.canJoin) {
                        availableServers.selectionModel.select(tmp)
                    }
                }
            }
        }
    }

    fun handleKeyboard(keyEvent: KeyEvent) {
        if (0 == StateProvider.getState().id) {
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
        val message = GameMessage.newBuilder()
            .setRoleChange(GameMessage.RoleChangeMsg.newBuilder().setSenderRole(NodeRole.VIEWER).build())
            .setSenderId(StateProvider.getState().id)
            .setMsgSeq(MessageIdProvider.getNextMessageId())
            .build()

        netWorker.putMessage(message, StateProvider.getState().serverAddress, StateProvider.getState().serverPort)
        StateProvider.getState().role = NodeRole.VIEWER
        StateProvider.getState().id = 0
        netWorker.subscribe(this, GameMessage.TypeCase.ACK)
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
        netWorker.subscribe(this, GameMessage.TypeCase.ACK)
        val joinMessage = GameMessage.newBuilder()
            .setJoin(
                GameMessage.JoinMsg.newBuilder().setName(SettingsProvider.getSettings().playerName).build()
            )
            .setMsgSeq(MessageIdProvider.getNextMessageId())
            .build()

        netWorker.putMessage(joinMessage, announceItem.ip, announceItem.port)
    }

    fun handleJoinGame() {
        if (StateProvider.getState().role != NodeRole.VIEWER && StateProvider.getState().serverPort > 0) {
            logger.warn { "Already joined game" }
            return
        }

        if (null != availableServers.selectionModel.selectedItem) {
            joinGame(availableServers.selectionModel.selectedItem)
        }
    }

    override fun update(message: Message) {
        when (message.msg.typeCase) {
            GameMessage.TypeCase.STATE -> {
                if ((StateProvider.getState().lastGameState?.stateOrder ?: -1) < message.msg.state.state.stateOrder) {
                    StateProvider.getState().lastGameState = message.msg.state.state
                }
            }
            GameMessage.TypeCase.ACK -> handleAck(message)
            GameMessage.TypeCase.ROLE_CHANGE -> handleRoleChange(message)
            GameMessage.TypeCase.ANNOUNCEMENT -> handleAnnounce(message)
            GameMessage.TypeCase.ERROR -> handleError(message)
            else -> return
        }
    }

    private fun handleError(message: Message) {
        val error = message.msg.error
    }

    private fun handleAnnounce(msg: Message) {
        availableServersBuffer.add(AnnounceItem.fromProto(msg))
    }

    private fun handleRoleChange(message: Message) {
        StateProvider.getState().role = message.msg.roleChange.receiverRole
        if (message.msg.roleChange.receiverRole == NodeRole.MASTER && !SnakeServerUtils.isRunning()) {
            logger.info { "Master left from game, trying to restore topology" }
            restoreServerLocal()
            return
        }

        if (message.msg.roleChange.senderRole == NodeRole.MASTER) {
            changeServer(message.ip, message.port)
        }
    }


    private fun restoreServerLocal() {
        SnakeServerUtils.restoreServer(StateProvider.getState().lastGameState!!)
    }

    private fun changeServer(newAddress: InetAddress, newPort: Int) {
        StateProvider.getState().serverAddress = newAddress
        StateProvider.getState().serverPort = newPort

        netWorker.clearQueue().forEach { message ->
            netWorker.putMessage(message.msg, newAddress, newPort)
        }
    }

    private fun handleAck(message: Message) {
        StateProvider.getState().serverAddress = message.ip
        StateProvider.getState().serverPort = message.port
        StateProvider.getState().id = message.msg.receiverId
        netWorker.unsubscribe(this, GameMessage.TypeCase.ACK)
    }

    private fun pingServer() {
        if (StateProvider.getState().role != NodeRole.VIEWER && StateProvider.getState().serverPort > 0) {
            val message = GameMessage.newBuilder().setPing(GameMessage.PingMsg.getDefaultInstance())
                .setMsgSeq(MessageIdProvider.getNextMessageId())
                .build()
            netWorker.putMessage(message, StateProvider.getState().serverAddress, StateProvider.getState().serverPort)
        }
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

        netWorker.subscribe(this, GameMessage.TypeCase.ROLE_CHANGE)
        netWorker.subscribe(this, GameMessage.TypeCase.STATE)
        netWorker.subscribe(this, GameMessage.TypeCase.ERROR)

        netWorker.subscribe(painter, GameMessage.TypeCase.STATE)
        netWorker.subscribe(painter, GameMessage.TypeCase.ERROR)

        announcer.subscribe(this, GameMessage.TypeCase.ANNOUNCEMENT)

        ThreadManager.addThread(netWorkerThread)
        ThreadManager.addThread(announcerThread)

        netWorkerThread.start()
        announcerThread.start()

        fixedRateTimer(
            name = "Core tick timer",
            daemon = true,
            initialDelay = 0L,
            period = SettingsProvider.getSettings().announceDelayMs.toLong()
        ) { fireAnnounceUpdate() }
        fixedRateTimer(
            name = "Client ping timer",
            daemon = true,
            initialDelay = 0L,
            period = SettingsProvider.getSettings().announceDelayMs.toLong()
        ) { pingServer() }

    }

}