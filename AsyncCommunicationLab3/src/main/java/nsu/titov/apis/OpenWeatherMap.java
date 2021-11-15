package nsu.titov.apis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import nsu.titov.engine.HttpServer;
import nsu.titov.models.OpenWeatherResponse;
import nsu.titov.models.Point;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.asynchttpclient.Response;

import java.util.function.Consumer;

public class OpenWeatherMap {
    private static final Logger logger = LogManager.getLogger(OpenWeatherMap.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static String url(Point point) {
        return String.format("%s/weather?lat=%f&lon=%f&units=metric&appid=%s",
                PropsProvider.openWeatherBaseUrl,
                point.lat,
                point.lon,
                PropsProvider.openWeatherApiKey);
    }

    public static void getWeatherByPoint(Point point,
                                         Consumer<OpenWeatherResponse> onSuccess,
                                         Consumer<Response> onError) {
        HttpServer.sendGetRequest(
                url(point),
                response -> {
                    try {
                        onSuccess.accept(parse(response));
                    } catch (JsonProcessingException e) {
                        logger.error("Unable to parse response body");
                        onError.accept(response);
                    }
                },
                response -> {
                    logger.error(String.format("Error occurred while requesting weather, code: %d", response.getStatusCode()));
                    onError.accept(response);
                }
        );
    }

    private static OpenWeatherResponse parse(Response rawResponse) throws JsonProcessingException {
        return objectMapper.readValue(rawResponse.getResponseBody(), OpenWeatherResponse.class);
    }


}
