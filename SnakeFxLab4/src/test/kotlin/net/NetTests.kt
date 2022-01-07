package net

import nsu.titov.net.StubEndpoint
import nsu.titov.net.client.ClientThreadNetWorker
import org.junit.Test
import kotlin.concurrent.thread

class NetTests {
    @Test
    fun testAck() {
        val netWorker = ClientThreadNetWorker(StubEndpoint(50))
        val thread = thread(start = true, isDaemon = false, name = "Hui") {
            netWorker.run()
        }


        netWorker.shutdown()
        thread.join()
    }

}