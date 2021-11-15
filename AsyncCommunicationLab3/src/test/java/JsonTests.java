import com.fasterxml.jackson.databind.ObjectMapper;
import nsu.titov.apis.PropsProvider;
import nsu.titov.models.GraphHopperResponse;
import nsu.titov.models.OpenTripInfoResponse;
import nsu.titov.models.OpenTripResponse;
import nsu.titov.models.OpenWeatherResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;

public class JsonTests {

    @BeforeAll
    public static void disableLogging() {
        java.util.logging.Logger.getLogger("org.apache.http.wire").setLevel(java.util.logging.Level.FINEST);
        java.util.logging.Logger.getLogger("org.apache.http.headers").setLevel(java.util.logging.Level.FINEST);
        System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog");
        System.setProperty("org.apache.commons.logging.simplelog.showdatetime", "true");
        System.setProperty("org.apache.commons.logging.simplelog.log.httpclient.wire", "ERROR");
        System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.http", "ERROR");
        System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.http.headers", "ERROR");
    }

    @Test
    public void testGraphhopperJson() throws IOException {
        try (InputStream input = PropsProvider.class.getResourceAsStream("/graphhopper.json")) {
            var objectMapper = new ObjectMapper();
            var response = objectMapper.readValue(input, GraphHopperResponse.class);
            var item = response.items.get(0);

            Assertions.assertEquals(item.point.lat, 53.61674335);
            Assertions.assertEquals(item.point.lon, 108.13072518745162);
            Assertions.assertEquals(item.name, "Lake Baikal");
            Assertions.assertEquals(item.country, "Russia");
            Assertions.assertEquals(item.countryCode, "RU");
        }
    }

    @Test
    public void testWeatherJson() throws IOException {
        try (InputStream input = PropsProvider.class.getResourceAsStream("/weather.json")) {
            var objectMapper = new ObjectMapper();
            var response = objectMapper.readValue(input, OpenWeatherResponse.class);

            Assertions.assertEquals(response.main.feelsLike, -12.29);
            Assertions.assertEquals(response.main.temp, -5.39);
            Assertions.assertEquals(response.wind.speed, 6);
        }
    }

    @Test
    public void testPlacesJson() throws IOException {
        try (InputStream input = PropsProvider.class.getResourceAsStream("/opentrip.json")) {
            var objectMapper = new ObjectMapper();
            var response = objectMapper.readValue(input, OpenTripResponse.class);

            var item = response.get(0);
            Assertions.assertEquals(item.xid, "R555716");
            Assertions.assertEquals(item.name, "Baikal lake");
            Assertions.assertEquals(item.wikidata, "Q5513");
        }
    }

    @Test
    public void testInfoJson() throws IOException {
        try (InputStream input = PropsProvider.class.getResourceAsStream("/info.json")) {
            var objectMapper = new ObjectMapper();
            var response = objectMapper.readValue(input, OpenTripInfoResponse.class);

            Assertions.assertEquals(response.info.description, "name");
            Assertions.assertEquals(response.name, "NAME");
        }


    }

}
