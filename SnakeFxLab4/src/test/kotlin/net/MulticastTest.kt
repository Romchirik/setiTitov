package net

import org.junit.Test
import java.net.InetAddress
import java.net.MulticastSocket

@Suppress("UNREACHABLE_CODE")
class MulticastTest {
    private val multicastIp: InetAddress = InetAddress.getByName("239.192.0.4")
    private val port = 9192

    private val sender = MulticastSocket(5784)
    private val receiver = MulticastSocket(port)

    @Test
    fun simpleTest() {
        val thread = Thread(MulticastReceiver())
        val publisher = MulticastPublisher()
        thread.start()

        publisher.multicast("Hui")
        Thread.sleep(2000);

        publisher.multicast("Hui")
        Thread.sleep(2000);
        publisher.multicast("Hui")
        Thread.sleep(2000);
        publisher.multicast("Hui")
        Thread.sleep(2000);
        publisher.multicast("Hui")
        Thread.sleep(2000);

    }
}