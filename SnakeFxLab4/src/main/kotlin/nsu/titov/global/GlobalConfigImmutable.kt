package nsu.titov.global

data class GlobalConfigImmutable(
    val width: Int,
    val height: Int,
    val foodStatic: Int,
    val foodPerPlayer: Float,
    val stateDelayMs: Int,
    val deadFoodProb: Float,
    val pingDelayMs: Int,
    val nodeTimeoutMs: Int
)
