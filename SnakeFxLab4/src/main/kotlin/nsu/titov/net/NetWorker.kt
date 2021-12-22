package nsu.titov.net

import nsu.titov.event.Publisher
import nsu.titov.proto.SnakeProto
import java.net.InetAddress

abstract class NetWorker : Publisher(), Runnable{
    abstract fun putMessage(message: SnakeProto.GameMessage, ip: InetAddress, port: Int)
    abstract fun stop()
}