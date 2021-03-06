package nsu.titov.utils

object UtilsConverters {
    fun intToBytes(data: Int): ByteArray {
        val buffer = ByteArray(Int.SIZE_BYTES)
        buffer[0] = (data shr 0).toByte()
        buffer[1] = (data shr 8).toByte()
        buffer[2] = (data shr 16).toByte()
        buffer[3] = (data shr 24).toByte()
        return buffer
    }

    fun bytesToInt(buffer: ByteArray): Int {
        return (buffer[3].toInt() shl 24) or
                (buffer[2].toInt() and 0xff shl 16) or
                (buffer[1].toInt() and 0xff shl 8) or
                (buffer[0].toInt() and 0xff)
    }

}