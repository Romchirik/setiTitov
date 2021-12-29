package nsu.titov.core

import nsu.titov.core.data.Playfield
import nsu.titov.core.data.Point
import nsu.titov.proto.SnakeProto
import nsu.titov.utils.dirToPoint
import nsu.titov.utils.pointToDir
import nsu.titov.utils.sign
import java.util.*
import kotlin.math.abs


class Snake : Collidable {
    private var snakeState: SnakeProto.GameState.Snake.SnakeState = SnakeProto.GameState.Snake.SnakeState.ALIVE
    private var playfield: Playfield
    var direction: SnakeProto.Direction = SnakeProto.Direction.UP
    private var body: LinkedList<Point> = LinkedList()

    /** Creates a snake from 2 points (points validation included), first point is interpreted as snake head,
     * second point is offset of snake tail
     * You can set offset only on one axis (x or y), for example (1, 0) (3, 0) will create a snake with length of 3,
     * head in (1, 0) and tail in (4, 0)
     *
     * @param head Snake head
     * @param offset Snake tail offset
     * @throws AssertionError if finds invalid offset
     */
    constructor (head: Point, offset: Point, playfield: Playfield) {
        this.playfield = playfield
        body.addAll(createSnakeSpan(head, offset))
        body.add(head + offset)
        direction = pointToDir(-offset)
        body.forEach { point -> playfield.normalizeDirty(point) }

    }

    /** Creates snake from starting points and offsets (offsets validation included), combined in list
     * @param points offsets list
     * @throws AssertionError if finds invalid offset
     */
    constructor (points: List<Point>, playfield: Playfield) {
        assert(points.size >= 2) { "Points list for snake must be longer than 2" }
        this.playfield = playfield
        var prevPoint = points.first()
        for (i in 1 until points.size) {
            body.addAll(createSnakeSpan(prevPoint, points[i]))
            prevPoint += points[i]
        }
        body.add(prevPoint)
        direction = pointToDir(-points[1])

        body.forEach { point -> playfield.normalizeDirty(point) }
    }

    override fun ifCollide(point: Point): Boolean {
        for (i in body) {
            if (i == point) {
                return true
            }
        }
        return false
    }


    /**
     * Moves snake forward, but needed field size represented as point to keep snake in field bounds
     * @param playfield current playfield info
     */
    fun tick() {
        body.removeLast()
        body.addFirst(playfield.normalize(body.first + dirToPoint(direction)))
    }

    fun grow() {
        val iter = body.descendingIterator()
        val tail1 = iter.next()
        val tail0 = iter.next()
        body.addLast(playfield.normalize(tail1 + (tail1 - tail0)))
    }

    /** Creates list of snake head point and offsets as in .proto representation
     * @param playfield current playfield
     */
    fun serialize(): List<Point> {
        val tmp: MutableList<Point> = mutableListOf(body.first);
        val offsetMap: MutableList<Point> = ArrayList();

        val iter0 = body.iterator()
        var prevPoint = iter0.next()
        while (iter0.hasNext()) {
            val currPoint = iter0.next()
            offsetMap.add(currPoint - prevPoint)
            prevPoint = currPoint
        }

        for (i in offsetMap) {
            if (abs(i.y) == playfield.height - 1) {
                i.y = -sign(i.y)
            } else if (abs(i.x) == playfield.width - 1) {
                i.x = -sign(i.x)
            }
        }

        val iter1 = offsetMap.iterator()
        var prevOffset = iter1.next()
        var acc = prevOffset
        while (iter1.hasNext()) {
            val currOffset = iter1.next()
            if (currOffset != prevOffset) {
                tmp.add(acc)
                acc = Point(0, 0)
            }
            acc += currOffset
            prevOffset = currOffset
        }
        tmp.add(acc)
        return tmp
    }

    fun setStateZombie() {
        snakeState = SnakeProto.GameState.Snake.SnakeState.ZOMBIE
    }

    fun getSize(): Int {
        return body.size
    }

    fun getBody(): List<Point> {
        return body
    }

    fun getState(): SnakeProto.GameState.Snake.SnakeState {
        return snakeState
    }

    fun getHead(): Point {
        return body.first
    }

    companion object {
        private fun createSnakeSpan(start: Point, offset: Point): List<Point> {
            assert(
                (offset.x == 0) xor (offset.y == 0)
            ) { "Trying to create new snake with wrong coords: x1: ${start.x}, x2: ${offset.x}, y1: ${start.y},y2: ${offset.y}" }

            val tmp = ArrayList<Point>()
            val step = Point(sign(offset.x), sign(offset.y))
            val delta = abs(if (offset.x == 0) offset.y else offset.x)

            for (i in 0 until delta) {
                tmp.add(start + (step * i))
            }
            return tmp
        }
    }
}
