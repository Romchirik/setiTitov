package nsu.titov.net

data class MessageWrapper(
    val message: Message,
    val firstSendTime: Long,
    var resendTime: Long
) {
}