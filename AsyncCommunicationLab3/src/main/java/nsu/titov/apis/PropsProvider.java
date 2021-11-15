package nsu.titov.apis;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;
import java.util.Properties;


public class PropsProvider {
    private static final Logger logger = LogManager.getLogger(PropsProvider.class);

    public static String graphHopperBaseUrl = null;
    public static String graphHopperApiKey = null;
    public static String openTripBaseUrl = null;
    public static String openTripApiKey = null;
    public static String openWeatherBaseUrl = null;
    public static String openWeatherApiKey = null;

    static {
        Properties props = new Properties();

        logger.debug("Initializing props manager");
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
}
