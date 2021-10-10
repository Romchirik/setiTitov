package nsu.titov.myproto

enum class MessageType(val numericCode: Byte) {
    INIT(0),
    DATA(1),
    FINISH(2),
    ERROR(3),
    ACCEPT(4),
    SUCCESS(5);

    companion object {
        fun fromByte(value: Byte) = values().first { it.numericCode == value }
    }
}
