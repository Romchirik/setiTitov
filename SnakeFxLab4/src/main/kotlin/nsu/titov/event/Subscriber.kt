package nsu.titov.event

import nsu.titov.net.Message

interface Subscriber {
    fun update(message: Message)
}