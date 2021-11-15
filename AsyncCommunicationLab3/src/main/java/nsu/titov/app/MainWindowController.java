package nsu.titov.app;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.web.WebView;
import nsu.titov.apis.GraphHopper;
import nsu.titov.apis.OpenTripMap;
import nsu.titov.apis.OpenWeatherMap;
import nsu.titov.models.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;
import java.util.ResourceBundle;

public class MainWindowController implements Initializable {
    private static final Logger logger = LogManager.getLogger(MainWindowController.class);
    @FXML
    public ListView<GraphHopperItem> searchResult;
    ObservableList<GraphHopperItem> locationItems = FXCollections.observableArrayList();
    @FXML
    public Label windSpeedLabel;
    @FXML
    public Label temperatureLabel;
    @FXML
    public Label selectedItemLabel;
    @FXML
    public Label errorMessage;
    @FXML
    public Label feelsLikeLabel;
    @FXML
    public ListView<OpenTripItem> interestingPlaces;
    ObservableList<OpenTripItem> interestingPlacesList = FXCollections.observableArrayList();
    @FXML
    public Button searchButton;
    @FXML
    public TextField placeInput;
    @FXML
    public Slider searchRadiusSlider;
    @FXML
    public Slider numResultsSlider;
    @FXML
    public WebView additionalInfo;

    @FXML
    public void handleShowAdditionalInfo() {
        if (interestingPlaces.getSelectionModel().getSelectedItem() == null) {
            setLabelText(errorMessage, "Select place before requesting additional info");
            return;
        }
        OpenTripMap.getInfoByXid(interestingPlaces.getSelectionModel().getSelectedItem().xid,
                this::showAdditionalInfo,
                response -> setLabelText(errorMessage, "Error occurred while requesting additional info"));

    }

    @FXML
    public void handleSearch() {
        if ("".equals(placeInput.getCharacters().toString())) {
            return;
        }
        GraphHopper.getPlacesByName(placeInput.getCharacters().toString(),
                this::setLocationsList,
                response -> setLabelText(errorMessage, "Error occurred while requesting locations")
        );
    }

    public void showAdditionalInfo(OpenTripInfoResponse info) {
        Platform.runLater(() -> additionalInfo.getEngine().loadContent(info.info == null ? "No info loaded" : info.info.description));

    }

    public void handleLocationSelected(GraphHopperItem item) {
        if (item == null) {
            return;
        }
        setLabelText(selectedItemLabel, item.name);
        OpenWeatherMap.getWeatherByPoint(item.point,
                this::setWeather,
                response -> setLabelText(errorMessage, "Error occurred while requesting weather")
        );
        OpenTripMap.getPlacesByPoint(item.point,
                (int) searchRadiusSlider.getValue() * Settings.KILOMETERS_MULTIPLIER,
                (int) numResultsSlider.getValue(),
                this::setPlacesList,
                response -> setLabelText(errorMessage, "Error occurred while requesting weather")
        );
    }

    private void setPlacesList(OpenTripResponse response) {
        Platform.runLater(() -> {
            interestingPlacesList.clear();
            interestingPlacesList.addAll(response);
        });
    }

    private void setWeather(OpenWeatherResponse response) {
        setLabelText(temperatureLabel, String.format("%.1f", response.main.temp));
        setLabelText(feelsLikeLabel, String.format("%.1f", response.main.feelsLike));
        setLabelText(windSpeedLabel, String.format("%.1f m/s", response.wind.speed));
    }

    void setLocationsList(GraphHopperResponse response) {
        Platform.runLater(() -> {
            locationItems.clear();
            locationItems.addAll(response.items);
        });
    }

    private void setLabelText(Label label, String text) {
        Platform.runLater(() -> label.setText(text));
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        numResultsSlider.setValue(Settings.DEFAULT_RESULT_LIMITER);

        searchRadiusSlider.setValue(Settings.DEFAULT_SEARCH_RADIUS_KM);

        searchResult.setItems(locationItems);
        searchResult.setEditable(false);
        searchResult.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        searchResult.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> handleLocationSelected(newValue)
        );

        interestingPlaces.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        interestingPlaces.setItems(interestingPlacesList);
        interestingPlaces.setEditable(false);
    }
}
