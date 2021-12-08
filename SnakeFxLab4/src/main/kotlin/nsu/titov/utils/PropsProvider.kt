package nsu.titov.utils

import java.util.*
import kotlin.system.exitProcess


object PropsProvider {
    var defaultPingDelayMs: Int = 800
    var mainScreenTitle: String = "Hui"

    init {
        val props = Properties()

        try {
            PropsProvider::class.java.getResourceAsStream("/settings.properties").use { input ->
                props.load(input)
                mainScreenTitle = props.getProperty("main_screen_title")
            }
        } catch (e: Exception) {

            exitProcess(1)
        }
    }
}