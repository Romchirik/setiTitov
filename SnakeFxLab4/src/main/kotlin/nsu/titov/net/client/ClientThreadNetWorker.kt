package nsu.titov.net.client

import nsu.titov.net.*
import nsu.titov.net.crutch.ErrorManager
import nsu.titov.proto.SnakeProto
import nsu.titov.settings.SettingsProvider
import nsu.titov.utils.MessageIdProvider
import java.net.InetAddress
import java.util.*
import java.util.concurrent.ConcurrentLinkedDeque


class ClientThreadNetWorker : NetWorker {


    private val outgoingQueue: Deque<Message> = ConcurrentLinkedDeque()

    private var pendingMessage: MessageWrapper? = null
    private var running = true

    constructor() : super(SocketEndpoint(0))

    constructor(connectionEndpoint: ConnectionEndpoint) : super(connectionEndpoint)

    override fun setEndpoint(endpoint: ConnectionEndpoint): NetWorker {
        this.endpoint = endpoint
        return this
    }

    override fun clearQueue(): Deque<Message> {
        val tmp = ArrayDeque(outgoingQueue)
        outgoingQueue.clear()
        return tmp
    }

    override fun run() {

        while (running && !Thread.interrupted()) {

            //sending messages
            if (outgoingQueue.isNotEmpty() && pendingMessage == null) {
                val message = outgoingQueue.poll()
                sendMessage(message)

                if (message.msg.typeCase != SnakeProto.GameMessage.TypeCase.ACK &&
                    message.msg.typeCase != SnakeProto.GameMessage.TypeCase.ANNOUNCEMENT
                ) {
                    pendingMessage = MessageWrapper(message, System.currentTimeMillis(), System.currentTimeMillis())
                }

            }


            //receiving messages
            val incMessage = receiveMessage()

            if (incMessage != null) {
                sendAck(incMessage)
                logger.trace { "Received new message type of: ${incMessage.msg.typeCase}, from ${incMessage.ip}" }
                notifyMembers(incMessage, incMessage.msg.typeCase)

                if (incMessage.msg.typeCase == SnakeProto.GameMessage.TypeCase.ACK) {
                    if (null != pendingMessage) {
                        if (pendingMessage!!.message.msg.msgSeq <= incMessage.msg.msgSeq) {
                            pendingMessage = null
                        }
                    }
                }
            }


            //checking for timeouts and resending
            var serverProblems = false
            if (pendingMessage != null) {
                if (System.currentTimeMillis() - pendingMessage!!.firstSendTime > SettingsProvider.getSettings().timeoutDelayMs) {
                    serverProblems = true
                } else {
                    if (System.currentTimeMillis() - pendingMessage!!.resendTime > SettingsProvider.getSettings().pingDelayMs) {
                        sendMessage(pendingMessage!!.message)
                        pendingMessage!!.resendTime = System.currentTimeMillis()
                    }
                }
            }

            if (serverProblems) {
                val error = SnakeProto.GameMessage.newBuilder().setError(
                    SnakeProto.GameMessage.ErrorMsg.newBuilder().setErrorMessage(
                        ErrorManager.wrap(-1)
                    )
                )
                    .setMsgSeq(MessageIdProvider.getNextMessageId())
                    .build()
                notifyMembers(Message(error, InetAddress.getLoopbackAddress(), -1), error.typeCase)
                pendingMessage = null
            }

        }
        shutdown()
    }

    @Synchronized
    override fun putMessage(message: SnakeProto.GameMessage, ip: InetAddress, port: Int) {
        outgoingQueue.push(Message(message, ip, port))
        logger.trace { "New message of type: ${message.typeCase} in queue, seq: ${message.msgSeq} " }
    }

    override fun shutdown() {
        running = false
        logger.info { "Shutting down client networker" }
    }
}