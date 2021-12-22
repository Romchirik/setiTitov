package nsu.titov.settings

import com.fasterxml.jackson.databind.ObjectMapper

object SettingsProvider {
    var settings: Settings? = null
        private set
        get() {
            if (field == null) {
                settings = loadSettings()
            }

            return field
        }

    private fun loadSettings(): Settings {
        val objectMapper = ObjectMapper()
        return objectMapper.readValue(SettingsProvider::class.java.getResource("/settings.json"), Settings::class.java)
    }

}