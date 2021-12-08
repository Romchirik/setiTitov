package nsu.titov.net

import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.SocketAddress

class SocketEndpoint : ConnectionEndpoint {

    private val socket: DatagramSocket

    constructor(port: Int, address: InetAddress) {
        socket = DatagramSocket(port, address)
    }

    constructor(port: Int) {
        socket = DatagramSocket(port)
    }

    constructor(socketAddress: SocketAddress) {
        socket = DatagramSocket(socketAddress)
    }

    override fun receive(buffer: DatagramPacket) {
        socket.receive(buffer)
    }

    override fun send(datagramPacket: DatagramPacket) {
        socket.send(datagramPacket)
    }

    override fun setSoTimeout(timeout: Int) {
        socket.soTimeout = timeout
    }

    override fun getSoTimeout(): Int {
        return socket.soTimeout
    }

    override fun close() {
        socket.close()
    }

}