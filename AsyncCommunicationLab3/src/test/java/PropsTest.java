import nsu.titov.apis.PropsProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class PropsTest {
    @Test
    public void propTest() {
        Assertions.assertEquals(PropsProvider.getGraphHopperApiKey(), "3cf7af7f-5135-4ff4-921b-8a8866414dc4");
        Assertions.assertEquals(PropsProvider.getGraphHopperBaseUrl(), "https://graphhopper.com/api/1");
        Assertions.assertEquals(PropsProvider.getOpenTripApiKey(), "5ae2e3f221c38a28845f05b6f96be01e7f0f6074cb1b7ef47fca6b08");
        Assertions.assertEquals(PropsProvider.getOpenTripBaseUrl(), "https://api.opentripmap.com/0.1");
        Assertions.assertEquals(PropsProvider.getOpenWeatherApiKey(), "8435275bda294926346465878f4551d1");
        Assertions.assertEquals(PropsProvider.getOpenWeatherBaseUrl(), "https://api.openweathermap.org/data/2.5");
    }
}
