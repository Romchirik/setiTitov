package nsu.titov;

import javafx.application.Application;
import nsu.titov.app.App;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main {
    private static final Logger logger = LogManager.getLogger(Main.class);

    public static void main(String[] args) {
        Application.launch(App.class, args);
    }
}
