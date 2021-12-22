package nsu.titov.server

import mu.KotlinLogging
import nsu.titov.event.Publisher
import nsu.titov.event.Subscriber
import nsu.titov.net.Message
import nsu.titov.net.NetWorker
import nsu.titov.net.SocketEndpoint
import nsu.titov.net.ThreadNetWorker
import nsu.titov.proto.SnakeProto
import java.net.InetAddress
import kotlin.concurrent.fixedRateTimer

class SnakeServer(private val serverConfig: ServerConfig) : Publisher(), Subscriber, Server {
    private val logger = KotlinLogging.logger {}

    private val timer = fixedRateTimer(name = "hui", daemon = false, initialDelay = 0L, period = 2000) { fireTick() }
    private val netWorker = ThreadNetWorker(SocketEndpoint(InetAddress.getLocalHost(), 6734))
    private val netWorkerThread = Thread(netWorker)
    private var running: Boolean = true

    init {

    }

    private fun handleJoin(message: SnakeProto.GameMessage) {

    }


    private fun fireTick() {
    }

    private fun handleError(message: SnakeProto.GameMessage) {
        val error = message.error
        logger.warn { "Error message from ${message.senderId}, message: ${error.errorMessage}" }
    }


    //TODO redo
    override fun update(message: Message) {

//        when (message.typeCase) {
//            SnakeProto.GameMessage.TypeCase.STEER -> notifyMembers(message, message.typeCase)
//            SnakeProto.GameMessage.TypeCase.JOIN -> handleJoin(message)
//            SnakeProto.GameMessage.TypeCase.ERROR -> handleError(message)
//            else -> logger.error { "Snake server received message of invalid type: ${message.typeCase}" }
//        }
    }

    override fun stop() {
        running = false

        timer.cancel()
        timer.purge()

    }


    override fun run() {
        netWorkerThread.start()

        while (running) {
            Thread.sleep(1000)
        }

        netWorker.stop()
        netWorkerThread.join()
    }

}