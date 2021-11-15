package nsu.titov.app;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import nsu.titov.engine.HttpServer;


public final class App extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("main_window.fxml"));
        stage.setTitle("Weather Trip Hopper");
        Parent view = loader.load();
        Scene tmp = new Scene(view);
        stage.setOnCloseRequest(evt -> {
            HttpServer.close();
            Platform.exit();
        });
        stage.setScene(tmp);
        stage.show();
    }
}
