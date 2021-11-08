package nsu.titov.apis;

import nsu.titov.Main;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


public class PropsProvider {
    private static PropsProvider instance = null;

    private static final Logger logger = LogManager.getLogger(PropsProvider.class);

    private String graphHopperBaseUrl = null;
    private String graphHopperApiKey = null;
    private String openTripBaseUrl = null;
    private String openTripApiKey = null;
    private String openWeatherBaseUrl = null;
    private String openWeatherApiKey = null;

    private PropsProvider(){
        Properties props = new Properties();

        logger.debug("initializing props manager");
        try (InputStream input = PropsProvider.class.getResourceAsStream("/api_keys.properties")) {
            props.load(input);
            graphHopperBaseUrl = props.getProperty("graphhopper_base_url");
            graphHopperApiKey = props.getProperty("graphhopper_key");
            openTripBaseUrl = props.getProperty("opentripmap_base_url");
            openTripApiKey = props.getProperty("opentripmap_key");
            openWeatherBaseUrl = props.getProperty("openweathermap_base_url");
            openWeatherApiKey = props.getProperty("openweathermap_key");
        } catch (Exception e) {
            logger.error(String.format("Error occurred while loading props %s", e));
            System.exit(1);
        }
    }
    public static PropsProvider getInstance() {
        if (instance == null) {
            instance = new PropsProvider();
        }
        return instance;
    }


    public static String getGraphHopperBaseUrl() {
        return getInstance().graphHopperBaseUrl;
    }

    public static String getGraphHopperApiKey() {
        return getInstance().graphHopperApiKey;
    }

    public static String getOpenTripBaseUrl() {
        return getInstance().openTripBaseUrl;
    }

    public static String getOpenTripApiKey() {
        return getInstance().openTripApiKey;
    }

    public static String getOpenWeatherBaseUrl() {
        return getInstance().openWeatherBaseUrl;
    }

    public static String getOpenWeatherApiKey() {
        return getInstance().openWeatherApiKey;
    }
}
