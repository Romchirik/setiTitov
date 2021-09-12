import java.net.DatagramPacket
import java.net.InetAddress
import java.net.MulticastSocket
import java.nio.charset.Charset
import java.util.*
import kotlin.collections.HashMap


class CopiesDetector(
    private val group: InetAddress,
    private val uid: UUID,
) {
    private var socket: MulticastSocket = MulticastSocket(Settings.port)
    var copiesData: HashMap<UUID, Boolean> = HashMap()

    init {

    }

    private fun removeCopy(uid: UUID) {
        copiesData.remove(uid)
    }

    private fun addCopy(uid: UUID) {
        copiesData[uid] = true
    }

    private fun checkCopy(uid: UUID): Boolean {
        return null != copiesData[uid] && false != copiesData[uid]
    }

    fun run() {
        var message = "ab hui"
        var bytes: ByteArray = message.toByteArray(Charset.forName("UTF8"))
        val hi = DatagramPacket(
            bytes, bytes.size,
            group, Settings.port
        )
        val buf = ByteArray(1000)
        val recv = DatagramPacket(buf, buf.size)
        socket.joinGroup(group)
        println("Started")

        while (true) {
            socket.send(hi)
            Thread.sleep(1000)
            socket.receive(recv)
            println(recv.data.toString(Charset.forName("UTF8")))

        }

    }
}