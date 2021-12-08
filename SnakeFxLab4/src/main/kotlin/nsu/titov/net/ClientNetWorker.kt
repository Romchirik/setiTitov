package nsu.titov.net

import mu.KotlinLogging
import nsu.titov.proto.SnakeProto
import java.net.InetAddress
import java.net.InetSocketAddress
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.Semaphore

class ClientNetWorker() : Runnable {

    private val logger = KotlinLogging.logger {}
    private var endpoint: ConnectionEndpoint = StubEndpoint()
    private val outgoingQueue: AbstractQueue<SnakeProto.GameMessage> = ConcurrentLinkedQueue()
    private val incomingQueue: AbstractQueue<SnakeProto.GameMessage> = ConcurrentLinkedQueue()

    //TODO(Remove magic constant)
    private val smhToDoSem: Semaphore = Semaphore(100);

    fun putMessage(message: SnakeProto.GameMessage) {
        logger.debug { "New message in queue msgSeq: ${message.msgSeq}, messageType: ${message.typeCase}" }
        outgoingQueue.add(message);
    }

    fun popMessage(): SnakeProto.GameMessage? {
        return incomingQueue.poll()
    }

    fun setEndpoint(endpoint: ConnectionEndpoint) {
        this.endpoint = endpoint
    }

    override fun run() {
        val stub = InetSocketAddress(InetAddress.getByName("localhost"), 6589)
        while (true) {
            if (!outgoingQueue.isEmpty()) {

            }
        }
    }
}