package nsu.titov.event

import nsu.titov.net.Message
import nsu.titov.proto.SnakeProto
import java.util.concurrent.Semaphore

open class Publisher {
    private val subscribers: MutableMap<SnakeProto.GameMessage.TypeCase, MutableList<Subscriber>> = HashMap()
    private val removeQueue = ArrayDeque<Subscriber>()

    private val queueLock = Semaphore(1)

    init {
        SnakeProto.GameMessage.TypeCase.values().forEach { case ->
            subscribers[case] = ArrayList()
        }
    }

    fun subscribe(sub: Subscriber, type: SnakeProto.GameMessage.TypeCase) {
        subscribers[type]?.add(sub)
    }

    fun unsubscribe(sub: Subscriber, type: SnakeProto.GameMessage.TypeCase) {
        if(queueLock.tryAcquire()) {
            subscribers[type]?.remove(sub)
        } else {
            removeQueue.add(sub)
        }
    }

    fun notifyMembers(data: Message, type: SnakeProto.GameMessage.TypeCase) {
        queueLock.acquire()
        subscribers[type]?.forEach { subscriber -> subscriber.update(data) }
        queueLock.release()
        if (removeQueue.isNotEmpty()) {
            for (sub in removeQueue) {
                subscribers[type]?.remove(sub)
            }
            removeQueue.clear()
        }

    }
}