package nsu.titov.client

import mu.KotlinLogging
import nsu.titov.event.Publisher
import nsu.titov.net.Message
import nsu.titov.proto.SnakeProto
import nsu.titov.settings.SettingsProvider
import java.net.DatagramPacket
import java.net.InetAddress
import java.net.MulticastSocket
import java.net.SocketTimeoutException
import java.util.*

class AnnounceHandler() : Publisher(), Runnable {
    private val multicastAddress: InetAddress
    private val multicastPort: Int
    private val socket: MulticastSocket

    private var running: Boolean = true
    private val logger = KotlinLogging.logger {}


    init {
        val settings = SettingsProvider.getSettings()
        multicastAddress = InetAddress.getByName(settings.multicastAddress)
        multicastPort = settings.multicastPort
        socket = MulticastSocket(settings.multicastPort)
    }


    override fun run() {
        initialize()
        while (running) {
            val packet = DatagramPacket(ByteArray(BUFFER_SIZE), BUFFER_SIZE)
            try {
                socket.soTimeout = SettingsProvider.getSettings().announceDelayMs
                socket.receive(packet)
            } catch (_: SocketTimeoutException) {
                continue
            }

            val message = SnakeProto.GameMessage.parseFrom(Arrays.copyOf(packet.data, packet.length))!!
            if (message.typeCase != SnakeProto.GameMessage.TypeCase.ANNOUNCEMENT) {
                logger.error { "Announcer received not an announce message" }
                continue
            }
            notifyMembers(Message(message, packet.address, packet.port), SnakeProto.GameMessage.TypeCase.ANNOUNCEMENT)
        }
    }

    private fun initialize() {
        socket.joinGroup(multicastAddress)
    }

    fun stop() {
        socket.leaveGroup(multicastAddress)
        running = false
    }

    companion object {
        private const val BUFFER_SIZE: Int = 4096
    }
}