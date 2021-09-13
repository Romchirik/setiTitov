import java.net.DatagramPacket
import java.net.InetAddress
import java.net.MulticastSocket
import java.net.SocketTimeoutException
import java.sql.Time
import java.util.*


class CopiesDetector(
    private val group: InetAddress,
    private val uid: UUID,
) {
    private var inputBuffer: ByteArray = ByteArray(Constants.MESSAGE_SIZE)

    private var working: Boolean = true
    private var socket: MulticastSocket = MulticastSocket(Constants.port)
    private var copiesData: HashMap<UUID, Long> = HashMap()

    init {
        socket.soTimeout = 1000
    }

    fun run() {
        socket.joinGroup(group)
        println("Started")
        while (working) {
            update()
            Thread.sleep(1000)
        }
        terminate()
    }

    private fun terminate() {
        sendMessage(Message(uid, MessageType.LEAVE))
        socket.leaveGroup(group)
    }

    private fun sendMessage(message: Message) {
        val serializedMessage = MessageBuilderUtil.serializeMessage(message)
        val packet = DatagramPacket(
            serializedMessage, serializedMessage.size,
            group, Constants.port
        )
        socket.send(packet)
    }

    private fun update() {
        sendMessage(Message(uid, MessageType.DATA))
        val datagramPacket = DatagramPacket(inputBuffer, inputBuffer.size)
        try {
            while (true) {
                socket.receive(datagramPacket)
                val incomingMessage = MessageBuilderUtil.buildMessage(datagramPacket.data)
                handleMessage(incomingMessage)
            }
        } catch (e: SocketTimeoutException) {
            checkTimeouts()
        }
        println("Number of copies online: ${copiesData.size - 1} ")
    }

    private fun checkTimeouts() {
        val filtered =
            copiesData.filter { (key, value) ->
                value - System.currentTimeMillis() < Constants.DISCONNECT_TIMEOUT || key == uid
            }
        copiesData.clear()

        filtered.forEach { copiesData[it.key] = it.value }
    }

    private fun handleMessage(message: Message) {
        if (message.type == MessageType.LEAVE) {
            copiesData.remove(message.uuid)
            println("Leave message received")
        } else {
            copiesData[message.uuid] = System.currentTimeMillis()
        }
    }

    fun stop() {
        working = false
    }
}