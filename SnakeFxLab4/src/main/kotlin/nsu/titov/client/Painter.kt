package nsu.titov.client

import nsu.titov.event.Subscriber
import nsu.titov.proto.SnakeProto

interface Painter: Subscriber {
    fun repaint(state: SnakeProto.GameMessage.StateMsg)
    fun addAvailableSever(server: SnakeProto.GameMessage.AnnouncementMsg)
}