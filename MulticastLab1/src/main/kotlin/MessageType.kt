enum class MessageType(val value: Int) {
    DATA(0),
    LEAVE(1);

    companion object {
        fun fromInt(value: Int) = values().first { it.value == value }
    }
}