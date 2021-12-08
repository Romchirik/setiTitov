package nsu.titov.app

import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.stage.Stage
import mu.KotlinLogging
import nsu.titov.utils.PropsProvider


class App : Application() {
    private val logger = KotlinLogging.logger {}

    override fun start(stage: Stage?) {
        logger.info { "Starting ui" }

        val loader = FXMLLoader(javaClass.classLoader.getResource("main_screen.fxml"))
        stage!!.title = PropsProvider.mainScreenTitle
        val view = loader.load<Parent>()
        val tmp = Scene(view)
        stage.scene = tmp
        stage.show()
    }

}