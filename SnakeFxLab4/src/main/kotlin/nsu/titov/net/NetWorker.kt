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


    companion object {
        internal const val TIMEOUT: Int = 20
        internal const val BUFFER_SIZE: Int = 8192
    }
}