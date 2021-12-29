package nsu.titov.core.data

data class CoreConfig(
    val width: Int = 40,
    val height: Int = 30,
    val foodStatic: Int = 1,
    val foodPerPlayer: Float = 1f,
    val deadFoodProbe: Float = .1f
)
