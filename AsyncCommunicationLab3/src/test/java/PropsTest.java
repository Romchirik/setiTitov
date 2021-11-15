import nsu.titov.apis.PropsProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class PropsTest {
    @Test
    public void propTest() {
        Assertions.assertEquals(PropsProvider.graphHopperApiKey, "graphhopper");
        Assertions.assertEquals(PropsProvider.graphHopperBaseUrl, "https://graphhopper.com/api/1");
        Assertions.assertEquals(PropsProvider.openTripApiKey, "opentripmap");
        Assertions.assertEquals(PropsProvider.openTripBaseUrl, "https://api.opentripmap.com/0.1");
        Assertions.assertEquals(PropsProvider.openWeatherApiKey, "openweather");
        Assertions.assertEquals(PropsProvider.openWeatherBaseUrl, "https://api.openweathermap.org/data/2.5");
    }
}
