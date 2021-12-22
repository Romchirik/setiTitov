package net

import nsu.titov.net.StubEndpoint
import nsu.titov.net.ThreadNetWorker
import org.junit.Test
import kotlin.concurrent.thread

class NetTests {
    @Test
    fun testAck() {
        val netWorker = ThreadNetWorker(StubEndpoint(50))
        val thread = thread(start = true, isDaemon = false, name = "Hui") {
            netWorker.run()
        }


        netWorker.stop()
        thread.join()
    }

}