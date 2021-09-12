enum class MessageType(val value: Int) {
    JOIN(1),
    DATA(2),
    LEAVE(3);

    companion object {
        fun fromInt(value: Int) = MessageType.values().first { it.value == value }
    }
}