package nsu.titov.client

import nsu.titov.proto.SnakeProto

interface Controller {
    fun setErrorMessage(message: String)
    fun handleMessage(message: SnakeProto.GameMessage)
}