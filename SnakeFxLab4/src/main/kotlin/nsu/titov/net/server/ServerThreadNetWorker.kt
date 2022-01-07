package nsu.titov.net.server

import nsu.titov.net.*
import nsu.titov.net.crutch.ErrorManager
import nsu.titov.proto.SnakeProto
import nsu.titov.settings.SettingsProvider
import nsu.titov.utils.MessageIdProvider
import java.net.InetAddress
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedDeque

class ServerThreadNetWorker : NetWorker {
    private val messageQueue: MutableMap<InetAddress, Deque<Message>> = ConcurrentHashMap()
    private val pendingMessages: MutableMap<InetAddress, MessageWrapper> = HashMap()
    private val lastMsgSeq: MutableMap<InetAddress, Long> = ConcurrentHashMap()

    private var running: Boolean = true

    constructor() : super(SocketEndpoint(0))
    constructor(connectionEndpoint: ConnectionEndpoint) : super(connectionEndpoint)

    @Synchronized
    override fun putMessage(message: SnakeProto.GameMessage, ip: InetAddress, port: Int) {
        val wrapper = Message(message, ip, port)
        if (!messageQueue.containsKey(ip)) {
            messageQueue[ip] = ConcurrentLinkedDeque()
        }
        messageQueue[ip]!!.push(wrapper)

    }

    override fun shutdown() {
        running = false
        logger.debug { "Network thread will be finished as soon as possible" }
    }


    override fun setEndpoint(endpoint: ConnectionEndpoint): NetWorker {
        this.endpoint = endpoint
        return this
    }

    override fun clearQueue(): Deque<Message> {
        return ConcurrentLinkedDeque()
    }


    override fun run() {
        while (running) {
            for (queue in messageQueue) {
                if (queue.value.isNotEmpty() && pendingMessages[queue.key] == null) {
                    val message = queue.value.poll()
                    if (message.msg.typeCase == SnakeProto.GameMessage.TypeCase.ACK) {
                        println("Hui")
                    }
                    sendMessage(message)
                    if (message.msg.typeCase != SnakeProto.GameMessage.TypeCase.ACK &&
                        message.msg.typeCase != SnakeProto.GameMessage.TypeCase.ANNOUNCEMENT
                    ) {
                        pendingMessages[message.ip] =
                            MessageWrapper(message, System.currentTimeMillis(), System.currentTimeMillis())
                    }

                }
            }

            //receiving messages
            val incMsg = receiveMessage()

            if (incMsg != null) {
                sendAck(incMsg)
                logger.trace { "Received new message type of: ${incMsg.msg.typeCase}, from ${incMsg.ip}" }
                if (incMsg.msg.typeCase == SnakeProto.GameMessage.TypeCase.ACK) {
                    val pending = pendingMessages[incMsg.ip]
                    if (pending != null) {
                        if (pending.message.msg.msgSeq <= incMsg.msg.msgSeq) {
                            pendingMessages.remove(incMsg.ip)
                        }
                    }
                }
                notifyMembers(incMsg, incMsg.msg.typeCase)
            }

            //check for timeouts
            val disconnected: MutableList<Message> = ArrayList()
            pendingMessages.forEach { (_, wrapper) ->
                if (System.currentTimeMillis() - wrapper.firstSendTime > SettingsProvider.getSettings().timeoutDelayMs) {
                    disconnected.add(wrapper.message)
                } else {
                    if (System.currentTimeMillis() - wrapper.resendTime > SettingsProvider.getSettings().pingDelayMs) {
                        sendMessage(wrapper.message)
                        wrapper.resendTime = System.currentTimeMillis()
                    }
                }
            }

            disconnected.forEach { player ->
                if (!player.msg.hasReceiverId()) {
                    logger.error { "Id not found for message: ${player.msg}" }
                    return
                }
                logger.info { "Disconnecting player: ${player.msg.receiverId}" }
                val error = SnakeProto.GameMessage.newBuilder().setError(
                    SnakeProto.GameMessage.ErrorMsg.newBuilder().setErrorMessage(
                        ErrorManager.wrap(player.msg.receiverId)
                    )
                )
                    .setMsgSeq(MessageIdProvider.getNextMessageId())
                    .build()
                notifyMembers(Message(error, InetAddress.getLoopbackAddress(), -1), error.typeCase)
                pendingMessages.remove(player.ip)
                messageQueue.remove(player.ip)
            }
        }
    }
}