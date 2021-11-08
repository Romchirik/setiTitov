package nsu.titov.apis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import nsu.titov.engine.HttpServer;
import nsu.titov.models.OpenTripInfoResponse;
import nsu.titov.models.OpenTripResponse;
import nsu.titov.models.Point;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.asynchttpclient.Response;

import java.util.function.Consumer;

public class OpenTripMap {
    private static final Logger logger = LogManager.getLogger(OpenTripMap.class);

    private static String urlInfo(String xid) {
        return String.format("%s/en/places/xid/%s?apikey=%s",
                PropsProvider.getOpenTripBaseUrl(),
                xid,
                PropsProvider.getOpenTripApiKey()
        );
    }

    private static String urlPoint(Point point, int radius, int limit) {
        return String.format("%s/en/places/radius?radius=%d&lon=%f&lat=%f&format=json&limit=%d&apikey=%s",
                PropsProvider.getOpenTripBaseUrl(),
                radius,
                point.lon,
                point.lat,
                limit,
                PropsProvider.getOpenTripApiKey());
    }

    private static OpenTripResponse parse2(Response rawResponse) throws JsonProcessingException {
        var objectMapper = new ObjectMapper();
        return objectMapper.readValue(rawResponse.getResponseBody(), OpenTripResponse.class);
    }

    private static OpenTripInfoResponse parse1(Response rawResponse) throws JsonProcessingException {
        var objectMapper = new ObjectMapper();
        return objectMapper.readValue(rawResponse.getResponseBody(), OpenTripInfoResponse.class);
    }

    public static void getPlacesByPoint(Point point, int radius, int limit,
                                        Consumer<OpenTripResponse> onSuccess,
                                        Consumer<Response> onError) {
        HttpServer.sendGetRequest(
                urlPoint(point, radius, limit),
                response -> {
                    try {
                        onSuccess.accept(parse2(response));
                    } catch (JsonProcessingException e) {
                        logger.error("Unable to parse response body");
                        onError.accept(response);
                    }
                },
                response -> {
                    logger.error(String.format("Error occurred while requesting interesting places, code: %d", response.getStatusCode()));
                    onError.accept(response);
                }
        );
    }


    public static void getInfoByXid(String xid,
                                    Consumer<OpenTripInfoResponse> onSuccess,
                                    Consumer<Response> onError) {
        HttpServer.sendGetRequest(
                urlInfo(xid),
                response -> {
                    try {
                        onSuccess.accept(parse1(response));
                    } catch (JsonProcessingException e) {
                        logger.error("Unable to parse response body");
                        onError.accept(response);
                    }
                },
                response -> {
                    logger.error(String.format("Error occurred while requesting place info, code: %d", response.getStatusCode()));
                    onError.accept(response);
                }
        );
    }
}
