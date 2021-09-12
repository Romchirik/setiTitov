import java.net.InetAddress
import java.util.*

fun main(args: Array<String>) {
    val myUUID: UUID = UUID.randomUUID();
    val copiesDetector: CopiesDetector = CopiesDetector(InetAddress.getByName(Settings.ip), myUUID)
    copiesDetector.run()
}