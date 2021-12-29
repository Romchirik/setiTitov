package nsu.titov.server

interface Server: Runnable{
    fun stop()
    fun getPort(): Int
}