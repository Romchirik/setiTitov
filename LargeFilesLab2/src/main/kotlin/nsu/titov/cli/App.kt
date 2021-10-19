package nsu.titov.cli

import mu.KotlinLogging
import nsu.titov.client.Client
import nsu.titov.server.Server
import nsu.titov.utils.Settings.DEFAULT_PORT
import org.hibernate.validator.constraints.Range
import picocli.CommandLine.Command
import picocli.CommandLine.Option
import java.io.File
import java.net.ConnectException
import java.util.concurrent.Callable

@Command(name = "mp-transmitter", description = ["Simple my-protocol file transmitter"])
class App : Callable<Int> {

    @Option(names = ["-f", "--file"], paramLabel = "FILE", description = ["transmit this file to server"])
    var targetFile: File? = null

    @Option(
        names = ["-r", "--receive"],
        description = ["receive files (start as server), if on flag passed, start as client"]
    )
    var receive: Boolean = false

    @Range(min = 1, max = 65535, message = "Valid port range: from 1 to 65535")
    @Option(
        names = ["-p", "--port"],
        description = ["server port, if starting as server, overrides default server port"]
    )
    var port: Int = DEFAULT_PORT

    @Option(names = ["-a", "--address"], description = ["server address (only for client)"])
    var address: String = "127.0.0.1"

    @Option(names = ["-h", "--help"], usageHelp = true, description = ["print this help screen"])
    var help = false

    private val logger = KotlinLogging.logger {}

    override fun call(): Int {
        if (receive) {
            try {
                Server(port).run()
            } catch (e: Throwable) {
                logger.error { "Unable to start server: $e" }
                return 0
            }
        } else {
            if (targetFile == null) {
                logger.warn { "No file passed" }
                return 0
            }
            if (!targetFile!!.isFile || !targetFile!!.exists()) {
                logger.warn { "Invalid file" }
                return 0
            }
            try {
                Client(address, port, targetFile!!).run()
            } catch (e: ConnectException) {
                logger.warn { "Unable to establish connection: connection rejected" }
                return 0
            }
        }
        return 0
    }
}