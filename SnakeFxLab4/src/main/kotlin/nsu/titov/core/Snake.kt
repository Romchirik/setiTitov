package nsu.titov.core

import nsu.titov.proto.SnakeProto
import nsu.titov.utils.*
import java.util.*
import kotlin.math.abs


class Snake {
    var body: LinkedList<SnakeNode> = LinkedList()
    private var direction = SnakeProto.Direction.UP

    /** Creates a snake from 2 points (points validation included), first point is interpreted as snake head,
     * second point is offset of snake tail
     * You can set offset only on one axis (x or y), for example (1, 0) (3, 0) will create a snake with length of 3,
     * head in (1, 0) and tail in (4, 0)
     *
     * @param head Snake head
     * @param offset Snake tail offset
     */
    constructor (head: Point, offset: Point) {
        body.addAll(createSnakeSpan(head, offset))
        body.add(SnakeNode(false, head + offset))
        body[0].isHead = true
        direction = pointToDir(offset)
    }

    /** Creates snake from incoming protobuf message
     *
     * @see SnakeProto.GameState.Snake
     * @param message Snake tail offset
     */
    constructor (message: SnakeProto.GameState.Snake) {
        var prevPoint = coordToPoint(message.pointsList[0])
        for (i in 1 until message.pointsCount) {
            body.addAll(createSnakeSpan(prevPoint, coordToPoint(message.pointsList[i])))
            prevPoint += coordToPoint(message.pointsList[i])
        }
        body.add(SnakeNode(false, prevPoint))
        body[0].isHead = true
        direction = message.headDirection
    }

    fun setDir(direction: SnakeProto.Direction) {
        this.direction = direction
    }


    fun grow() {
        val offset = body[body.size - 1].coordinates - body[body.size - 2].coordinates
        body.add(SnakeNode(false, body[body.size - 1].coordinates + offset))
    }

    fun tick() {
        val tail = body.removeLast()
        tail.isHead = true
        tail.coordinates = body.first.coordinates + dirToPoint(direction)
        body.first.isHead = false
        body.addFirst(tail)
    }

    fun toProto(fieldConfig: FieldConfig): SnakeProto.GameState.Snake.Builder {
        val nodePoints = ArrayList<Point>()
        val iter = body.iterator()

        var prevNode = iter.next()
        nodePoints.add(prevNode.coordinates)
        val tmp = iter.next()
        var prevDir = pointToDir(
            Point(
                x = (tmp.coordinates - prevNode.coordinates).x % fieldConfig.width,
                y = (tmp.coordinates - prevNode.coordinates).y % fieldConfig.height
            )
        )
        prevNode = tmp
        lateinit var currNode: SnakeNode
        while (iter.hasNext()) {
            currNode = iter.next()
            val currDir = pointToDir(
                Point(
                    x = (currNode.coordinates - prevNode.coordinates).x % fieldConfig.width,
                    y = (currNode.coordinates - prevNode.coordinates).y % fieldConfig.height
                )
            )
            if (currDir != prevDir) {
                nodePoints.add(prevNode.coordinates)
            }
            prevDir = currDir
            prevNode = currNode
        }
        nodePoints.add(prevNode.coordinates)
        val message = SnakeProto.GameState.Snake.newBuilder().setHeadDirection(direction)

        val listIter = nodePoints.listIterator()
        var prevPoint = listIter.next()
        message.addPoints(pointToCoord(nodePoints[0]))
        while (listIter.hasNext()) {
            val currPoint = listIter.next()
            message.addPoints(pointToCoord(currPoint - prevPoint))
            prevPoint = currPoint
        }
        return message
    }

    fun getSize(): Int {
        return body.size
    }

    data class SnakeNode(
        var isHead: Boolean = false,
        var coordinates: Point = Point(0, 0)
    )

    companion object {
        private fun createSnakeSpan(start: Point, offset: Point): List<SnakeNode> {
            assert(
                (offset.x == 0) xor (offset.y == 0)
            ) { "Trying to create new snake with wrong coords: x1: ${start.x}, x2: ${offset.x}, y1: ${start.y},y2: ${offset.y}" }

            val tmp = ArrayList<SnakeNode>()
            val step = Point(sign(offset.x), sign(offset.y))
            val delta = abs(if (offset.x == 0) offset.y else offset.x)

            for (i in 0 until delta) {
                tmp.add(SnakeNode(false, start + (step * i)))
            }
            return tmp
        }
    }

}


