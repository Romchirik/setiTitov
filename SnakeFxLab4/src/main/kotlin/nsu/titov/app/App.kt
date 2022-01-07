package nsu.titov.app

import javafx.application.Application
import javafx.event.EventHandler
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.stage.Stage
import mu.KotlinLogging
import nsu.titov.server.SnakeServerUtils
import nsu.titov.settings.SettingsProvider
import nsu.titov.utils.ThreadManager


class App : Application() {
    private val logger = KotlinLogging.logger {}

    override fun start(stage: Stage?) {
        logger.info { "Starting ui" }

        val loader = FXMLLoader(javaClass.classLoader.getResource("main_screen.fxml"))
        stage!!.title = SettingsProvider.getSettings().mainWindowTitle
        val view = loader.load<Parent>()
        val tmp = Scene(view)


        stage.onCloseRequest = EventHandler {
            run {
                SnakeServerUtils.stopServer()
                ThreadManager.shutdown()
            }
        }
        stage.scene = tmp
        stage.show()
    }

}