package nsu.titov.utils

import nsu.titov.core.data.Point
import nsu.titov.proto.SnakeProto

fun coordToPoint(coord: SnakeProto.GameState.Coord): Point {
    return Point(coord.x, coord.y)
}

fun pointToCoord(point: Point): SnakeProto.GameState.Coord {
    return SnakeProto.GameState.Coord.newBuilder()
        .setX(point.x)
        .setY(point.y)
        .build()
}

fun pointToDir(point: Point): SnakeProto.Direction {
    assert(
        (point.x == 0) xor (point.y == 0)
    ) { "Unable to create dir for point x: ${point.x}, y: ${point.y}" }


    return if (point.x > 0) {
        SnakeProto.Direction.RIGHT
    } else if (point.x < 0) {
        SnakeProto.Direction.LEFT
    } else if (point.y > 0) {
        SnakeProto.Direction.DOWN
    } else {
        SnakeProto.Direction.UP
    }
}

fun dirToPoint(direction: SnakeProto.Direction): Point {
    return when (direction) {
        SnakeProto.Direction.UP -> Point(0, -1)
        SnakeProto.Direction.DOWN -> Point(0, 1)
        SnakeProto.Direction.LEFT -> Point(-1, 0)
        SnakeProto.Direction.RIGHT -> Point(1, 0)
    }
}

fun invertDir(direction: SnakeProto.Direction): SnakeProto.Direction {
    return when (direction) {
        SnakeProto.Direction.UP -> SnakeProto.Direction.DOWN
        SnakeProto.Direction.DOWN -> SnakeProto.Direction.UP
        SnakeProto.Direction.LEFT -> SnakeProto.Direction.RIGHT
        SnakeProto.Direction.RIGHT -> SnakeProto.Direction.LEFT
    }
}