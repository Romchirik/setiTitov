package nsu.titov

import javafx.application.Application
import nsu.titov.app.App


fun main(args: Array<String>) {
    Application.launch(App::class.java, *args)
//    val thread = Thread(MulticastReceiver())
//    val publisher = MulticastPublisher()
//    thread.start()
//
//    publisher.multicast("Hui11111111")
//    Thread.sleep(2000);
//
//    publisher.multicast("Hui11111111")
//
//    Thread.sleep(2000);
//    publisher.multicast("Hui11111111")
//    Thread.sleep(2000);
//    publisher.multicast("Hui11111111")
//    Thread.sleep(2000);
//    publisher.multicast("end")
//    Thread.sleep(2000);
}