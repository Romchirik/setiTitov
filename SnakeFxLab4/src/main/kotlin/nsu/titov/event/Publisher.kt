package nsu.titov.event

import nsu.titov.net.Message
import nsu.titov.proto.SnakeProto

open class Publisher {
    private val subscribers: MutableMap<SnakeProto.GameMessage.TypeCase, MutableList<Subscriber>> = HashMap()

    init {
        SnakeProto.GameMessage.TypeCase.values().forEach { case ->
            subscribers[case] = ArrayList()
        }
    }

    fun subscribe(sub: Subscriber, type: SnakeProto.GameMessage.TypeCase) {
        subscribers[type]?.add(sub)
    }

    fun unsubscribe(sub: Subscriber, type: SnakeProto.GameMessage.TypeCase) {
        subscribers[type]?.remove(sub)
    }

    fun notifyMembers(data: Message, type: SnakeProto.GameMessage.TypeCase) {
        subscribers[type]?.forEach{subscriber -> subscriber.update(data) }
    }
}