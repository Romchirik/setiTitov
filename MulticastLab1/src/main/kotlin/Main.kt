@file:JvmName("App")

import sun.misc.Signal
import java.net.InetAddress
import java.util.*

fun main(args: Array<String>) {
    val myUUID: UUID = UUID.randomUUID();
    val copiesDetector = CopiesDetector(InetAddress.getByName(Settings.ip), myUUID)
    Signal.handle(Signal("INT")) {
        copiesDetector.stop()
    }
    copiesDetector.run()
}