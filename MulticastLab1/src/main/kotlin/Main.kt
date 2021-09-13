@file:JvmName("App")

import sun.misc.Signal
import java.net.InetAddress
import java.net.UnknownHostException
import java.util.*

fun main(args: Array<String>) {
    val myUUID: UUID = UUID.randomUUID();
    Constants.ip = args[0]
    try {
        val copiesDetector = CopiesDetector(InetAddress.getByName(Constants.ip), myUUID)
        Signal.handle(Signal("INT")) {
            copiesDetector.stop()
        }
        copiesDetector.run()
    } catch (e: UnknownHostException){
        println("Invalid ip");
    }
}