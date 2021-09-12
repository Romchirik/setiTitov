import java.nio.ByteBuffer

object ByteUtils {
    fun longToBytes(x: Long): ByteArray? {
        val buffer = ByteBuffer.allocate(java.lang.Long.BYTES)
        buffer.putLong(x)
        return buffer.array()
    }

    fun bytesToLong(bytes: ByteArray?): Long {
        val buffer = ByteBuffer.allocate(java.lang.Long.BYTES)
        buffer.put(bytes)
        buffer.flip() //need flip
        return buffer.long
    }
}