package nsu.titov.server

import mu.KotlinLogging
import nsu.titov.client.StateProvider
import nsu.titov.core.GameCore
import nsu.titov.core.SnakeGameCore
import nsu.titov.core.data.CoreConfig
import nsu.titov.event.Publisher
import nsu.titov.event.Subscriber
import nsu.titov.net.Message
import nsu.titov.net.NetWorker
import nsu.titov.net.crutch.ErrorManager
import nsu.titov.net.server.ServerThreadNetWorker
import nsu.titov.proto.SnakeProto
import nsu.titov.settings.SettingsProvider
import nsu.titov.utils.MessageIdProvider
import nsu.titov.utils.PlayerIdProvider
import java.net.InetAddress
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit

class SnakeServer(private val serverConfig: ServerConfig) : Publisher(), Subscriber, Server {
    private var gameCore: GameCore

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
    private val players: MutableMap<Int, ServerPlayerInfo> = ConcurrentHashMap()

    private var masterId: Int = 0
    private val timersPool = ScheduledThreadPoolExecutor(1)

    private val multicastAddress = InetAddress.getByName(SettingsProvider.getSettings().multicastAddress)
    private val multicastPort = SettingsProvider.getSettings().multicastPort


    private fun fireAnnounce() {
        val announce = AnnounceBuilder.getBuilder()
            .addPlayers(players.filter { players -> players.value.role != SnakeProto.NodeRole.VIEWER })
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
        gameCore.getPlayers().forEach { player ->
            players[player.key]!!.score = player.value.score
        }
        val state = StateBuilder.getBuilder()
            .setConfig(serverConfig)
            .setSnakes(gameCore.getSnakes())
            .setFoods(gameCore.getFoods())
            .setPlayers(players)
            .build()


        val gameMsgBuilder = SnakeProto.GameMessage.newBuilder()
            .setMsgSeq(MessageIdProvider.getNextMessageId())
            .setState(state)
        players.forEach { (_, player) ->
            netWorker.putMessage(gameMsgBuilder.setReceiverId(player.id).build(), player.addressInet, player.port)
        }

        players.forEach { (id, _) ->
            if (!gameCore.getPlayers().containsKey(id)) {
                setViewer(id)
            }
        }

    }

    private fun setViewer(id: Int) {
        if (players[id]!!.role == SnakeProto.NodeRole.DEPUTY) {
            selectNewDeputy()
            players[id]!!.role = SnakeProto.NodeRole.NORMAL
        }


        val message = SnakeProto.GameMessage.newBuilder()
            .setRoleChange(
                SnakeProto.GameMessage.RoleChangeMsg.newBuilder().setReceiverRole(SnakeProto.NodeRole.VIEWER)
                    .build()
            )
            .setReceiverId(id)
            .setMsgSeq(MessageIdProvider.getNextMessageId()).build()

        netWorker.putMessage(message, players[id]!!.addressInet, players[id]!!.port)
        players[id]!!.role = SnakeProto.NodeRole.VIEWER
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

        if (setMaster) {
            masterId = newId
            players[newId]!!.role = SnakeProto.NodeRole.MASTER
        }

        val roleChange = SnakeProto.GameMessage.RoleChangeMsg.newBuilder()
            .setReceiverRole(if (setMaster) SnakeProto.NodeRole.MASTER else SnakeProto.NodeRole.NORMAL)
        val message0 = SnakeProto.GameMessage.newBuilder()
            .setRoleChange(roleChange)
            .setMsgSeq(MessageIdProvider.getNextMessageId())
            .build()
        netWorker.putMessage(message0, message.ip, message.port)

        logger.info { "New player accepted id: $newId" }

        if (players.size == 2) {
            selectNewDeputy()
        }
    }


    private fun handleError(message: Message) {
        val error = message.msg.error
        if (ErrorManager.isServiceError(error.errorMessage)) {
            val id = ErrorManager.fromString(error.errorMessage)
            disconnectPlayer(id)
            return
        } else {
            logger.error { "Received error message: ${error.errorMessage}" }
        }
    }

    private fun disconnectPlayer(id: Int) {
        when (players[id]!!.role) {
            SnakeProto.NodeRole.MASTER -> {
                logger.error { "I literally doesnt know what should happen if server trying to disconnect master, gl hf" }
            }
            SnakeProto.NodeRole.DEPUTY -> {
                selectNewDeputy()
            }
            else -> {}
        }
        players.remove(id)
        gameCore.removePlayer(id)
        logger.info { "Removed player with id $id" }
    }


    //TODO redo
    override fun update(message: Message) {
        when (message.msg.typeCase) {
            SnakeProto.GameMessage.TypeCase.STEER -> handleSteer(message)
            SnakeProto.GameMessage.TypeCase.JOIN -> handleJoin(message)
            SnakeProto.GameMessage.TypeCase.ERROR -> handleError(message)
            SnakeProto.GameMessage.TypeCase.ROLE_CHANGE -> handleRoleChange(message)
            else -> logger.error { "Snake server received message of invalid type: ${message.msg.typeCase}" }
        }
    }

    private fun handleRoleChange(message: Message) {
        val msg = message.msg
        when (msg.roleChange.senderRole) {
            SnakeProto.NodeRole.VIEWER -> {
                when (players[msg.senderId]!!.role) {
                    SnakeProto.NodeRole.NORMAL -> {
                        players[msg.senderId]?.role = msg.roleChange.senderRole
                        players.remove(msg.senderId)
                        gameCore.removePlayer(msg.senderId)
                    }
                    SnakeProto.NodeRole.DEPUTY -> {
                        selectNewDeputy()
                        players[msg.senderId]?.role = msg.roleChange.senderRole
                        players.remove(msg.senderId)
                        gameCore.removePlayer(msg.senderId)
                    }
                    SnakeProto.NodeRole.MASTER -> {
                        var deputy: ServerPlayerInfo? = null
                        players.forEach { (_, player) ->
                            if (player.role == SnakeProto.NodeRole.DEPUTY) deputy = player
                        }

                        if (deputy == null) {
                            shutdownServer()
                        } else {
                            logger.info { "Master left, trying to change topology" }
                            val changeAccept = SnakeProto.GameMessage.newBuilder()
                                .setRoleChange(
                                    SnakeProto.GameMessage.RoleChangeMsg.newBuilder()
                                        .setReceiverRole(SnakeProto.NodeRole.MASTER)
                                ).setMsgSeq(MessageIdProvider.getNextMessageId())
                                .setReceiverId(deputy!!.id)
                                .build()
                            netWorker.putMessage(changeAccept, deputy!!.addressInet, deputy!!.port)
                        }

                    }
                    else -> {
                        logger.error {
                            "Unexpected state change, player: ${msg.senderId} trying to " +
                                    "change role from ${players[msg.senderId]!!.role} to ${msg.roleChange.senderRole}"
                        }
                        return
                    }
                }
            }
            else -> {
                logger.warn { "Client ${msg.senderId} trying to change role not to viewer" }
                return
            }
        }
        val changeAccept = SnakeProto.GameMessage.newBuilder()
            .setRoleChange(
                SnakeProto.GameMessage.RoleChangeMsg.newBuilder().setReceiverRole(message.msg.roleChange.senderRole)
            ).setMsgSeq(MessageIdProvider.getNextMessageId())
            .setReceiverId(message.msg.senderId)
            .build()
        netWorker.putMessage(changeAccept, message.ip, message.port)

    }

    private fun shutdownServer() {

    }


    private fun selectNewDeputy() {
        for (pair in players) {
            if (pair.value.role == SnakeProto.NodeRole.NORMAL) {
                val message = SnakeProto.GameMessage.newBuilder()
                    .setRoleChange(
                        SnakeProto.GameMessage.RoleChangeMsg.newBuilder().setSenderRole(SnakeProto.NodeRole.DEPUTY)
                            .build()
                    )
                    .setSenderId(masterId)
                    .setReceiverId(pair.value.id)
                    .setMsgSeq(MessageIdProvider.getNextMessageId())
                    .build()

                netWorker.putMessage(message, pair.value.addressInet, pair.value.port)
                pair.value.role = SnakeProto.NodeRole.DEPUTY
                logger.info { "Deputy changed to id: ${pair.value.id}" }
                break
            }
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

        netWorker.shutdown()
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
        netWorker.subscribe(this, SnakeProto.GameMessage.TypeCase.ROLE_CHANGE)
    }

    private constructor(serverConfig: ServerConfig, core: GameCore, masterId: Int) : this(serverConfig) {
        this.masterId = masterId
        this.gameCore = core
    }

    companion object {
        fun fromProto(state: SnakeProto.GameState, masterId: Int): SnakeServer {
            //TODO not yet implemented
            val serverConfig = ServerConfig(
                stateTickDelayMs = state.config.stateDelayMs,
                pingDelayMs = state.config.pingDelayMs,
                timeoutDelayMs = state.config.nodeTimeoutMs,
                playfieldWidth = state.config.width,
                playfieldHeight = state.config.height
            )
            val newServer = SnakeServer(serverConfig, SnakeGameCore.fromProtoState(state), masterId)
            state.players.playersList.forEach { gamePlayer ->
                newServer.players[gamePlayer.id] = ServerPlayerInfo(
                    name = gamePlayer.name,
                    id = gamePlayer.id,
                    address = gamePlayer.ipAddress,
                    port = gamePlayer.port,
                    role = if (StateProvider.getState().id == gamePlayer.id) SnakeProto.NodeRole.MASTER else gamePlayer.role,
                    score = gamePlayer.score,
                    playerType = gamePlayer.type,
                    connected = true
                )
            }
            newServer.masterId = StateProvider.getState().id

            val notifyBuilder = SnakeProto.GameMessage.newBuilder()
                .setRoleChange(
                    SnakeProto.GameMessage.RoleChangeMsg.newBuilder().setSenderRole(SnakeProto.NodeRole.MASTER)
                        .build()
                )
                .setMsgSeq(MessageIdProvider.getNextMessageId())

            newServer.players.forEach { (id, player) ->
                if (player.role != SnakeProto.NodeRole.DEPUTY) {
                    newServer.netWorker.putMessage(
                        notifyBuilder.setReceiverId(id).build(),
                        player.addressInet,
                        player.port
                    )
                }
            }

            return newServer
        }
    }
}