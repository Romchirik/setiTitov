package nsu.titov.net

import java.net.DatagramPacket
import java.net.SocketTimeoutException

class StubEndpoint(private val lag: Int) : ConnectionEndpoint {

    override var soTimeout: Int = 50

    override fun receive(buffer: DatagramPacket) {
        Thread.sleep(lag.toLong())
        throw SocketTimeoutException("Timeout lol")
    }

    override fun send(datagramPacket: DatagramPacket) {
    }

    override fun close() {
    }
}