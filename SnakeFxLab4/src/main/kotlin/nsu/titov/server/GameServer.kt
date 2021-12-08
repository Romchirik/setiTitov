package nsu.titov.server

import nsu.titov.global.GlobalConfigImmutable

class GameServer : Server {

    //TODO(Remove magic constants)
    val port: UShort = 4475u;
    val tickDelay = 1000 //ms
    var running: Boolean = true;

    override fun setPort(port: UShort) {
        TODO("Not yet implemented")
    }

    override fun setConfig(config: GlobalConfigImmutable) {
        TODO("Not yet implemented")
    }


    override fun stop() {
        running = false
    }

    override fun run() {
        while (running) {
            TODO("Not yet implemented")
        }
    }
}