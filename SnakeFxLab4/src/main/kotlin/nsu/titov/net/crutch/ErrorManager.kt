package nsu.titov.net.crutch

object ErrorManager {
    fun fromString(target: String): Int {
        return target.substringAfter("%%", "-1").toInt()
    }

    fun isServiceError(target: String): Boolean {
        return target.startsWith("%%")
    }

    fun wrap(id: Int): String {
        return "%%$id"
    }
}

