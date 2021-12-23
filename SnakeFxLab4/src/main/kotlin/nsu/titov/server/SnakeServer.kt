package nsu.titov.server

import mu.KotlinLogging
import nsu.titov.core.GameCore
import nsu.titov.core.SnakeGameCore
import nsu.titov.core.data.Point
import nsu.titov.event.Publisher
import nsu.titov.event.Subscriber
import nsu.titov.net.Message
import nsu.titov.net.SocketEndpoint
import nsu.titov.net.ThreadNetWorker
import nsu.titov.proto.SnakeProto
import nsu.titov.settings.SettingsProvider
import nsu.titov.utils.MessageIdProvider
import nsu.titov.utils.PlayerIdProvider
import java.net.InetAddress
import java.util.*
import kotlin.concurrent.fixedRateTimer

class SnakeServer(private val serverConfig: ServerConfig) : Publisher(), Subscriber, Server {
    private val gameCore: GameCore

    init {
        gameCore = SnakeGameCore(
            Point(
                x = serverConfig.fieldWidth,
                y = serverConfig.fieldHeight
            )
        )
    }

    private val logger = KotlinLogging.logger {}

    private val netWorker = ThreadNetWorker(SocketEndpoint(InetAddress.getLocalHost(), 6734))
    private val netWorkerThread = Thread(netWorker, "Server net worker")
    private var running: Boolean = true
    private val players: MutableMap<Int, ServerPlayerInfo> = HashMap()


    //TODO переделать на тредпул
    private lateinit var coreTimer: Timer
    private lateinit var pingTimer: Timer
    private lateinit var announceTimer: Timer


    private val multicastAddress = InetAddress.getByName(SettingsProvider.getSettings().multicastAddress)
    private val multicastPort = SettingsProvider.getSettings().multicastPort

    private fun fireAnnounce() {
        val config = SnakeProto.GameConfig.newBuilder()
            .setWidth(serverConfig.fieldWidth)
            .setHeight(serverConfig.fieldHeight)
            .build()


        val playersTmp = SnakeProto.GamePlayers.newBuilder()
        this.players.forEach { (_, player) ->
            playersTmp.addPlayers(player.toProto())
        }

        //TODO add canJoin check
        val announce = SnakeProto.GameMessage.AnnouncementMsg.newBuilder()
            .setPlayers(playersTmp)
            .setConfig(config)
            .setCanJoin(true).build()

        val message = SnakeProto.GameMessage.newBuilder()
            .setAnnouncement(announce)
            .setMsgSeq(MessageIdProvider.getNextMessageId()).build()

        netWorker.putMessage(message, multicastAddress, multicastPort)
    }

    private fun firePing() {

    }

    private fun fireTick() {
        gameCore.tick()
    }

    private fun handleJoin(message: Message) {
        logger.debug { "New client trying to join game!" }

        val newId = PlayerIdProvider.getNextPlayerId()

        val player = ServerPlayerInfo(
            name = message.msg.join.name,
            role = SnakeProto.NodeRole.NORMAL,
            id = newId,
            address = message.ip,
            port = message.port,
            connected = true
        )

        players[newId] = player

        val response = SnakeProto.GameMessage.newBuilder()
            .setAck(SnakeProto.GameMessage.AckMsg.getDefaultInstance())
            .setReceiverId(newId)
            .setMsgSeq(MessageIdProvider.getNextMessageId())
            .build()

        netWorker.putMessage(response, message.ip, message.port)

        logger.info { "New player accepted id: $newId" }
    }


    private fun handleError(message: Message) {

    }


    //TODO redo
    override fun update(message: Message) {
        when (message.msg.typeCase) {
            SnakeProto.GameMessage.TypeCase.STEER -> notifyMembers(message, message.msg.typeCase)
            SnakeProto.GameMessage.TypeCase.JOIN -> handleJoin(message)
            SnakeProto.GameMessage.TypeCase.ERROR -> handleError(message)
            else -> logger.error { "Snake server received message of invalid type: ${message.msg.typeCase}" }
        }
    }


    override fun run() {
        initialize()


        while (running) {
            Thread.sleep(1000)
        }

        netWorker.stop()
        netWorkerThread.join()
    }

    override fun stop() {
        running = false

        pingTimer.cancel()
        pingTimer.purge()

        coreTimer.cancel()
        coreTimer.purge()

        announceTimer.cancel()
        announceTimer.purge()
    }

    private fun initialize() {
        coreTimer =
            fixedRateTimer(
                name = "Core tick timer",
                daemon = false,
                initialDelay = 0L,
                period = serverConfig.stateTickDelayMs.toLong()
            ) { fireTick() }
        pingTimer =
            fixedRateTimer(
                name = "Ping timer",
                daemon = false,
                initialDelay = 0L,
                period = serverConfig.stateTickDelayMs.toLong()
            ) { firePing() }
        announceTimer =
            fixedRateTimer(
                name = "Announce timer",
                daemon = false,
                initialDelay = 0L,
                period = serverConfig.stateTickDelayMs.toLong()
            ) { fireAnnounce() }
        netWorkerThread.start()
        netWorker.subscribe(this, SnakeProto.GameMessage.TypeCase.JOIN)
        netWorker.subscribe(this, SnakeProto.GameMessage.TypeCase.STEER)
        netWorker.subscribe(this, SnakeProto.GameMessage.TypeCase.ERROR)
    }


}