package net

import java.net.DatagramPacket

import java.net.InetAddress

import java.net.MulticastSocket


open class MulticastReceiver : Thread() {
    private var socket: MulticastSocket? = null
    private var buf = ByteArray(256)
    override fun run() {
        socket = MulticastSocket(4446)
        val group = InetAddress.getByName("230.0.0.0")
        socket!!.joinGroup(group)
        while (true) {
            val packet = DatagramPacket(buf, buf.size)
            socket!!.receive(packet)
            val received = String(
                packet.data, 0, packet.length
            )
            if ("end" == received) {
                break
            }
        }
        socket!!.leaveGroup(group)
        socket!!.close()
    }
}