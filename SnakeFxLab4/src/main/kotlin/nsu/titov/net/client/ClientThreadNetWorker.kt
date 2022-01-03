package nsu.titov.net.client

import nsu.titov.net.ConnectionEndpoint
import nsu.titov.net.Message
import nsu.titov.net.NetWorker
import nsu.titov.net.SocketEndpoint
import nsu.titov.proto.SnakeProto
import java.net.InetAddress
import java.util.*
import java.util.concurrent.ConcurrentLinkedDeque


class ClientThreadNetWorker : NetWorker {


    private val outgoingQueue: Deque<Message> = ConcurrentLinkedDeque()

    private var pendingMessage: Message? = null
    private var running = true

    constructor() : super(SocketEndpoint(0))

    constructor(connectionEndpoint: ConnectionEndpoint) : super(connectionEndpoint)

    override fun setEndpoint(endpoint: ConnectionEndpoint): NetWorker {
        this.endpoint = endpoint
        return this
    }

    override fun run() {
        while (running) {
            if (outgoingQueue.isNotEmpty() && pendingMessage == null) {
                val message = outgoingQueue.poll()
                sendMessage(message)
            }

            val message = receiveMessage()
            if (message != null) {
                sendAck(message)
                if (message.msg.typeCase == SnakeProto.GameMessage.TypeCase.ACK) {
                    if (pendingMessage != null) {
                        if (pendingMessage!!.msg.msgSeq <= message.msg.msgSeq) {
                            pendingMessage = null
                            logger.trace { "Message (msgSeq: ${message.msg.msgSeq}) confirmed" }
                        }
                    }
                }
                logger.trace { "Received new message type of: ${message.msg.typeCase}, from ${message.ip}" }
                notifyMembers(message, message.msg.typeCase)
            }
        }
    }

    @Synchronized
    override fun putMessage(message: SnakeProto.GameMessage, ip: InetAddress, port: Int) {
        outgoingQueue.push(Message(message, ip, port))
        logger.trace { "New message of type: ${message.typeCase} in queue, seq: ${message.msgSeq} " }
    }

    override fun stop() {
        running = false
        logger.info { "Network thread will be finished as soon as possible" }
    }

}