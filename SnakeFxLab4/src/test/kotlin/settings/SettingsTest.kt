package settings

import nsu.titov.settings.SettingsProvider

import org.junit.Test
import kotlin.test.assertEquals

class SettingsTest {

    @Test
    fun settingsTest(){
        val settings = SettingsProvider.getSettings()

        assertEquals(settings.screenSizeX, 1)
        assertEquals(settings.screenSizeY, 2)
        assertEquals(settings.mainWindowTitle, "Hui")

        assertEquals(settings.multicastAddress, "1.1")
        assertEquals(settings.multicastPort, 3)



    }
}