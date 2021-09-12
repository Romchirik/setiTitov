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
    private var socket: MulticastSocket = MulticastSocket(Settings.port)
    private var copiesData: HashMap<UUID, Long> = HashMap()

    init {
        socket.soTimeout = 1000
    }

    private fun removeCopy(uid: UUID) {
        copiesData.remove(uid)
    }

    private fun addCopy(uid: UUID) {
        copiesData[uid] = System.currentTimeMillis()
    }

    private fun checkCopy(uid: UUID): Boolean {
        return null != copiesData[uid] && 0L != copiesData[uid]
    }

    fun run() {
        System.currentTimeMillis()
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
            group, Settings.port
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
            copiesData.filter { it.value - System.currentTimeMillis() < Constants.DISCONNECT_TIMEOUT || it.key == uid }
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