package nsu.titov.server

import mu.KotlinLogging
import nsu.titov.core.GameCore
import nsu.titov.core.SnakeGameCore
import nsu.titov.core.data.CoreConfig
import nsu.titov.core.data.Point
import nsu.titov.event.Publisher
import nsu.titov.event.Subscriber
import nsu.titov.net.Message
import nsu.titov.net.NetWorker
import nsu.titov.net.server.ServerThreadNetWorker
import nsu.titov.proto.SnakeProto
import nsu.titov.settings.SettingsProvider
import nsu.titov.utils.MessageIdProvider
import nsu.titov.utils.PlayerIdProvider
import java.net.InetAddress
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit

class SnakeServer(private val serverConfig: ServerConfig) : Publisher(), Subscriber, Server {
    private val gameCore: GameCore

    init {
        gameCore = SnakeGameCore(
            CoreConfig(
                width = serverConfig.playfieldWidth,
                height = serverConfig.playfieldHeight
            )
        )
    }

    private val logger = KotlinLogging.logger {}

    private val netWorker: NetWorker = ServerThreadNetWorker()
    private val netWorkerThread = Thread(netWorker, "Server net worker")
    private var running: Boolean = true
    private val players: MutableMap<Int, ServerPlayerInfo> = HashMap()

    private val timersPool = ScheduledThreadPoolExecutor(1)

    private val multicastAddress = InetAddress.getByName(SettingsProvider.getSettings().multicastAddress)
    private val multicastPort = SettingsProvider.getSettings().multicastPort

    private fun fireAnnounce() {
        val announce = AnnounceBuilder.getBuilder()
            .addPlayers(players)
            .setJoin(true)
            .setServerConfig(serverConfig).build()

        val message = SnakeProto.GameMessage.newBuilder()
            .setAnnouncement(announce)
            .setMsgSeq(MessageIdProvider.getNextMessageId()).build()

        netWorker.putMessage(message, multicastAddress, multicastPort)
    }

    private fun firePing() {
        players.forEach { (id, player) ->
            val message = SnakeProto.GameMessage.newBuilder()
                .setPing(SnakeProto.GameMessage.PingMsg.newBuilder().build())
                .setReceiverId(id)
                .setMsgSeq(MessageIdProvider.getNextMessageId()).build()
            netWorker.putMessage(message, player.addressInet, player.port)
        }
    }

    private fun fireTick() {
        gameCore.tick()
        if (players.isEmpty()) {
            return
        }

        val state = StateBuilder.getBuilder()
            .setConfig(serverConfig)
            .setSnakes(gameCore.getSnakes())
            .setFoods(gameCore.getFoods())
            .build()

        val gameMsg = SnakeProto.GameMessage.newBuilder()
            .setMsgSeq(MessageIdProvider.getNextMessageId())
            .setState(state)
            .build()
        players.forEach { (_, player) ->
            netWorker.putMessage(gameMsg, player.addressInet, player.port)
        }


    }

    private fun handleJoin(message: Message) {
//        logger.debug { "New client trying to join game!" }

        val newId = PlayerIdProvider.getNextPlayerId()
        val setMaster = players.isEmpty()
        val player = ServerPlayerInfo(
            name = message.msg.join.name,
            role = SnakeProto.NodeRole.NORMAL,
            id = newId,
            address = message.ip.toString().removePrefix("/"),
            port = message.port,
            connected = true
        )

        players[newId] = player
        gameCore.addPlayer(newId, SnakeProto.PlayerType.HUMAN)

        val response = SnakeProto.GameMessage.newBuilder()
            .setAck(SnakeProto.GameMessage.AckMsg.getDefaultInstance())
            .setReceiverId(newId)
            .setMsgSeq(MessageIdProvider.getNextMessageId())
            .build()

        netWorker.putMessage(response, message.ip, message.port)


        val roleChange = SnakeProto.GameMessage.RoleChangeMsg.newBuilder()
            .setReceiverRole(if (setMaster) SnakeProto.NodeRole.MASTER else SnakeProto.NodeRole.NORMAL)
        val message0 = SnakeProto.GameMessage.newBuilder()
            .setRoleChange(roleChange)
            .setMsgSeq(MessageIdProvider.getNextMessageId())
            .build()
        netWorker.putMessage(message0, message.ip, message.port)

//        logger.info { "New player accepted id: $newId" }
    }


    private fun handleError(message: Message) {

    }


    //TODO redo
    override fun update(message: Message) {
        when (message.msg.typeCase) {
            SnakeProto.GameMessage.TypeCase.STEER -> handleSteer(message)
            SnakeProto.GameMessage.TypeCase.JOIN -> handleJoin(message)
            SnakeProto.GameMessage.TypeCase.ERROR -> handleError(message)
            else -> logger.error { "Snake server received message of invalid type: ${message.msg.typeCase}" }
        }
    }

    private fun handleSteer(message: Message) {
        val dir = message.msg.steer!!.direction
        val id = message.msg.senderId

        gameCore.putTurn(id, dir)
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
        timersPool.shutdown()
    }

    override fun getPort(): Int {
        return netWorker.getPort()
    }

    private fun initialize() {
        timersPool.scheduleAtFixedRate(
            { fireTick() },
            0L,
            serverConfig.stateTickDelayMs.toLong(),
            TimeUnit.MILLISECONDS
        )

        timersPool.scheduleAtFixedRate(
            { firePing() },
            0L,
            serverConfig.pingDelayMs.toLong(),
            TimeUnit.MILLISECONDS
        )
        timersPool.scheduleAtFixedRate(
            { fireAnnounce() },
            0L,
            SettingsProvider.getSettings().announceDelayMs.toLong(),
            TimeUnit.MILLISECONDS
        )

        netWorkerThread.start()
        netWorker.subscribe(this, SnakeProto.GameMessage.TypeCase.JOIN)
        netWorker.subscribe(this, SnakeProto.GameMessage.TypeCase.STEER)
        netWorker.subscribe(this, SnakeProto.GameMessage.TypeCase.ERROR)
    }
}