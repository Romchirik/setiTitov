package nsu.titov.server

interface Server: Runnable{
    fun shutdown()
    fun getPort(): Int
}