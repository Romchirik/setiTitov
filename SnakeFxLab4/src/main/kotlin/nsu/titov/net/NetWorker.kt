package nsu.titov.net

import mu.KotlinLogging
import nsu.titov.event.Publisher
import nsu.titov.proto.SnakeProto
import java.net.DatagramPacket
import java.net.InetAddress
import java.net.SocketTimeoutException
import java.util.*

abstract class NetWorker(protected var endpoint: ConnectionEndpoint) : Publisher(), Runnable {
    protected val logger = KotlinLogging.logger {}

    abstract fun putMessage(message: SnakeProto.GameMessage, ip: InetAddress, port: Int)
    abstract fun stop()
    abstract fun setEndpoint(endpoint: ConnectionEndpoint): NetWorker

    fun getPort(): Int {
        return endpoint.getPort()
    }

    protected fun receiveMessage(): Message? {
        val packet = DatagramPacket(ByteArray(BUFFER_SIZE), BUFFER_SIZE)

        try {
            endpoint.soTimeout = TIMEOUT
            endpoint.receive(packet)
        } catch (e: SocketTimeoutException) {
            return null
        }

        val message = SnakeProto.GameMessage.parseFrom(Arrays.copyOf(packet.data, packet.length))
        return Message(message, packet.address, packet.port)
    }

    protected fun sendMessage(message: Message) {
        val arr = message.msg.toByteArray()
        val packet = DatagramPacket(arr, arr.size, message.ip, message.port)
        endpoint.send(packet)
        logger.trace { "Message seq of ${message.msg.msgSeq} sent" }
    }

    protected fun sendAck(message: Message) {
        if (message.msg.typeCase == SnakeProto.GameMessage.TypeCase.JOIN ||
            message.msg.typeCase == SnakeProto.GameMessage.TypeCase.ACK ||
            message.msg.typeCase == SnakeProto.GameMessage.TypeCase.ANNOUNCEMENT
        ) {
            return
        }

        val ack = SnakeProto.GameMessage.newBuilder()
            .setAck(SnakeProto.GameMessage.AckMsg.getDefaultInstance())
            .setMsgSeq(message.msg.msgSeq)
            .build()

        sendMessage(Message(ack, message.ip, message.port))
    }


    companion object {
        internal const val TIMEOUT: Int = 20
        internal const val BUFFER_SIZE: Int = 8192
    }
}