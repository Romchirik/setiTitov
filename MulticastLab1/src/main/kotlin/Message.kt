import java.util.*

data class Message(
    var length: Int,
    var uid: UUID,
    var type: MessageType,
    var payload: String
) {
    override fun equals(other: Any?): Boolean {
        return if(this === other){
            true
        } else {
            if(other is Message){
                val tmp: Message = other
                payload == tmp.payload &&
                        type == tmp.type
            } else {
                false
            }
        }
    }

    override fun hashCode(): Int {
        var result = length
        result = 31 * result + uid.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + payload.hashCode()
        return result
    }
}