package nsu.titov.core

data class FieldConfig(
    var width: Int = 0,
    var height: Int = 0
) {

    fun normalizeCoordinates(point: Point): Point {
        var x: Int = point.x
        var y: Int = point.y

        if (x < 0) x += width
        if (y < 0) y += height

        if (x >= width) x %= width
        if (y >= height) y %= height

        return Point(x, y)
    }

    fun normalizeSnake(snake: Snake) {
        snake.body.first.coordinates = normalizeCoordinates(snake.body.first.coordinates)

    }

}
