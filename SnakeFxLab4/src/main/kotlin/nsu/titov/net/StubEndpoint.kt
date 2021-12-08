package nsu.titov.net

import java.net.DatagramPacket

class StubEndpoint : ConnectionEndpoint {
    private var timeout = 0;

    override fun receive(buffer: DatagramPacket) {
        Thread.sleep(timeout.toLong())
    }

    override fun send(datagramPacket: DatagramPacket) {
    }

    override fun setSoTimeout(timeout: Int) {
        this.timeout = timeout
    }

    override fun getSoTimeout(): Int {
        return timeout
    }

    override fun close() {
        TODO("Not yet implemented")
    }

}