package nsu.titov.server

import nsu.titov.global.GlobalConfigImmutable

interface Server: Runnable {
    fun setPort(port: UShort)
    fun setConfig(config: GlobalConfigImmutable)
    fun stop()
}