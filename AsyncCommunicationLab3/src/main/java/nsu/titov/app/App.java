package nsu.titov.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


public final class App extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("main_window.fxml"));
        stage.setTitle("Async Asshole Pain");
        Parent view = loader.load();
        Scene tmp = new Scene(view);
        stage.setScene(tmp);
        stage.show();
    }
}
