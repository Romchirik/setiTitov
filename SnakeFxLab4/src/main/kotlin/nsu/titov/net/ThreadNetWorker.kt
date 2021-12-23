package nsu.titov.net

import mu.KotlinLogging
import nsu.titov.proto.SnakeProto
import java.net.DatagramPacket
import java.net.InetAddress
import java.net.SocketTimeoutException
import java.util.*
import java.util.concurrent.ConcurrentLinkedDeque


class ThreadNetWorker(connectionEndpoint: ConnectionEndpoint) : NetWorker() {

    private var endpoint: ConnectionEndpoint
    private val logger = KotlinLogging.logger {}
    private val outgoingQueue: Deque<Message> = ConcurrentLinkedDeque()

    private var pendingMessage: Message? = null
    private var running = true

    init {
        this.endpoint = connectionEndpoint
    }

    fun setEndpoint(endpoint: ConnectionEndpoint): ThreadNetWorker {
        this.endpoint = endpoint
        return this
    }

    private fun receiveMessage(): Message? {
        val packet = DatagramPacket(ByteArray(BUFFER_SIZE), BUFFER_SIZE)

        endpoint.soTimeout = TIMEOUT
        try {
            endpoint.receive(packet)
        } catch (e: SocketTimeoutException) {
            return null
        }

        val message = SnakeProto.GameMessage.parseFrom(Arrays.copyOf(packet.data, packet.length))

        if (message.typeCase == SnakeProto.GameMessage.TypeCase.ACK) {
            if (pendingMessage != null) {
                if (pendingMessage!!.msg.msgSeq == message.msgSeq) {
                    pendingMessage = null
                    logger.debug { "Received ack for seq: ${message.msgSeq} " }
                }
            }
        } else if (message.typeCase != SnakeProto.GameMessage.TypeCase.JOIN) {
            val ack = SnakeProto.GameMessage.newBuilder().setAck(
                SnakeProto.GameMessage.AckMsg.getDefaultInstance()
            ).setMsgSeq(message!!.msgSeq).build().toByteArray()
            endpoint.send(DatagramPacket(ack, ack.size, packet.address, packet.port))
        }


        logger.debug { "Received new message type of: ${message.typeCase}, from ${packet.address}" }
        notifyMembers(Message(message, packet.address, packet.port), message.typeCase)
        return Message(message, packet.address, packet.port)
    }

    override fun run() {
        while (running) {
            if (outgoingQueue.isNotEmpty() && pendingMessage == null) {
                val message = outgoingQueue.poll()
                val arr = message.msg.toByteArray()
                val packet = DatagramPacket(arr, arr.size, message.ip, message.port)
                endpoint.send(packet)
                logger.debug { "Message seq of ${message.msg.msgSeq} sent" }
            }
            receiveMessage()
        }
    }

    @Synchronized
    override fun putMessage(message: SnakeProto.GameMessage, ip: InetAddress, port: Int) {
        outgoingQueue.push(Message(message, ip, port))
        logger.debug { "New message of type: ${message.typeCase} in queue, seq: ${message.msgSeq} " }
    }

    override fun stop() {
        running = false
        logger.debug { "Network thread will be finished as soon as possible" }
    }

    companion object {
        private const val TIMEOUT: Int = 20
        private const val BUFFER_SIZE: Int = 8192


        private var nextMessageId: Long = 0L
        fun getNextMessageId(): Long {
            nextMessageId++
            return nextMessageId - 1L
        }
    }
}