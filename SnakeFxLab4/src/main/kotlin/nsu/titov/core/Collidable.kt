package nsu.titov.core

import nsu.titov.core.data.Point

interface Collidable {
    fun ifCollide(point: Point): Boolean
}