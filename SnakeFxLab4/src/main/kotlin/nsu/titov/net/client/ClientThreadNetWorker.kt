package nsu.titov.net.client

import nsu.titov.net.ConnectionEndpoint
import nsu.titov.net.Message
import nsu.titov.net.NetWorker
import nsu.titov.net.SocketEndpoint
import nsu.titov.proto.SnakeProto
import java.net.DatagramPacket
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
                val arr = message.msg.toByteArray()
                val packet = DatagramPacket(arr, arr.size, message.ip, message.port)
                endpoint.send(packet)
                logger.debug { "Message seq of ${message.msg.msgSeq} sent" }
            }

            val msg = receiveMessage()
            if (msg != null) {
                if (msg.msg.typeCase == SnakeProto.GameMessage.TypeCase.ACK) {
                    if (pendingMessage != null) {
                        if (pendingMessage!!.msg.msgSeq == msg.msg.msgSeq) {
                            pendingMessage = null
                            logger.debug { "Received ack for seq: ${msg.msg.msgSeq} " }
                        }
                    }
                } else if (msg.msg.typeCase != SnakeProto.GameMessage.TypeCase.JOIN) {
                    val ack = SnakeProto.GameMessage.newBuilder().setAck(
                        SnakeProto.GameMessage.AckMsg.getDefaultInstance()
                    ).setMsgSeq(msg.msg.msgSeq).build().toByteArray()
                    endpoint.send(DatagramPacket(ack, ack.size, msg.ip, msg.port))
                }


                logger.debug { "Received new message type of: ${msg.msg.typeCase}, from ${msg.ip}" }
                notifyMembers(msg, msg.msg.typeCase)
            }
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

}