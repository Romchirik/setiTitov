package net

import java.io.IOException
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress


class MulticastPublisher {
    private var socket: DatagramSocket? = null
    private var group: InetAddress? = null
    private lateinit var buf: ByteArray

    @Throws(IOException::class)
    fun multicast(
        multicastMessage: String
    ) {
        socket = DatagramSocket()
        group = InetAddress.getByName("230.0.0.0")
        buf = multicastMessage.toByteArray()
        val packet = DatagramPacket(buf, buf.size, group, 4446)
        socket!!.send(packet)
        socket!!.close()
    }
}