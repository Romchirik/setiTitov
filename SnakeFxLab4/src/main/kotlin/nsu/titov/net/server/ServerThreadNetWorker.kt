package nsu.titov.net.server

import nsu.titov.net.*
import nsu.titov.proto.SnakeProto
import java.net.DatagramPacket
import java.net.InetAddress
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedDeque

class ServerThreadNetWorker : NetWorker {
    private val messageQueue: MutableMap<InetAddress, Deque<Message>> = ConcurrentHashMap()
    private val pendingMessages: MutableMap<InetAddress, MessageWrapper> = HashMap()

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

    override fun stop() {
        running = false
        logger.debug { "Network thread will be finished as soon as possible" }
    }


    override fun setEndpoint(endpoint: ConnectionEndpoint): NetWorker {
        this.endpoint = endpoint
        return this
    }


    override fun run() {
        while (running) {
            for (queue in messageQueue) {
                if (queue.value.isNotEmpty()) {
                    val message = queue.value.poll()
                    val arr = message.msg.toByteArray()
                    val packet = DatagramPacket(arr, arr.size, message.ip, message.port)
                    endpoint.send(packet)
                    logger.debug { "Message seq of ${message.msg.msgSeq} sent" }
                }
            }
            //receiving messages
            val incMsg = receiveMessage()
            if (incMsg != null) {
                logger.debug { "Received new message type of: ${incMsg.msg.typeCase}, from ${incMsg.ip}" }
                notifyMembers(incMsg, incMsg.msg.typeCase)
            }
        }


    }

}