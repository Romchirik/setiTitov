package nsu.titov.utils

fun sign(value: Int): Int {
    return if(value > 0) {
        1
    } else if (value < 0) {
        -1
    } else {
        0
    }
}