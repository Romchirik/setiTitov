package nsu.titov.apis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import nsu.titov.engine.HttpServer;
import nsu.titov.models.GraphHopperResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.asynchttpclient.Request;
import org.asynchttpclient.Response;

import java.util.function.Consumer;

import static org.asynchttpclient.Dsl.asyncHttpClient;
import static org.asynchttpclient.Dsl.get;

public class GraphHopper {
    private static final Logger logger = LogManager.getLogger(GraphHopper.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static String url(String place) {
        return String.format("%s/geocode?q=%s&locale=en&limit=20&key=%s",
                PropsProvider.graphHopperBaseUrl,
                place,
                PropsProvider.graphHopperApiKey);
    }

    private static GraphHopperResponse parse(Response rawResponse) throws JsonProcessingException {
        var objectMapper = new ObjectMapper();
        return objectMapper.readValue(rawResponse.getResponseBody(), GraphHopperResponse.class);
    }

    public static void getPlacesByName(String place,
                                       Consumer<GraphHopperResponse> onSuccess,
                                       Consumer<Response> onError) {
        logger.debug(String.format("Requesting locations from graphhopper, target: %s", place));
        HttpServer.sendGetRequest(
                url(place),
                response -> {
                    try {
                        onSuccess.accept(parse(response));
                    } catch (JsonProcessingException e) {
                        onError.accept(response);
                    }
                },
                response -> {
                    logger.error(String.format("Error occurred while requesting places, code: %d", response.getStatusCode()));
                    onError.accept(response);
                }
        );
    }


}
