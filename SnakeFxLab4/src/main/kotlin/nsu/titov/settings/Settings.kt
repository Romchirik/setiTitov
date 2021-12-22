package nsu.titov.settings

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class Settings {
    var screenSizeX = 1280
    var screenSizeY = 720
    var mainWindowTitle = "Snake FX"

    var multicastAddress = "239.192.0.4"
    var multicastPort = 6734

    var playerName = "Big flopper 228"
}