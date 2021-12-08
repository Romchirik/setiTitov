package nsu.titov.core

data class Point(
    var x: Int,
    var y: Int
) {
    operator fun plus(increment: Point): Point {
        return Point(this.x + increment.x, this.y + increment.y)
    }

    operator fun times(mul: Int): Point {
        return Point(this.x * mul, this.y * mul)
    }

    operator fun minus(coordinates: Point): Point {
        return Point(this.x - coordinates.x, this.y - coordinates.y)
    }

}
