package nsu.titov.apis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    private static String url(String place) {
        return String.format("%s/geocode?q=%s&locale=en&limit=20&key=%s",
                PropsProvider.getGraphHopperBaseUrl(),
                place,
                PropsProvider.getGraphHopperApiKey());
    }

    private static GraphHopperResponse parse(Response rawResponse) throws JsonProcessingException {
        var objectMapper = new ObjectMapper();
        return objectMapper.readValue(rawResponse.getResponseBody(), GraphHopperResponse.class);
    }

    public static void getPlacesByName(String place,
                                       Consumer<GraphHopperResponse> onSuccess,
                                       Consumer<Response> onError) {
        logger.debug(String.format("Requesting locations from graphhopper, target: %s", place));
        Request request = get(url(place)).build();
        asyncHttpClient()
                .executeRequest(request)
                .toCompletableFuture()
                .thenApply((response -> {
                    logger.debug(String.format("Requested locations form graphhopper, status code: %d", response.getStatusCode()));
                    if (200 == response.getStatusCode()) {
                        try {
                            var tmp = parse(response);
                            onSuccess.accept(tmp);
                        } catch (JsonProcessingException e) {
                            onError.accept(response);
                        }
                    } else {
                        onError.accept(response);
                    }
                    return response;
                }));

    }


}
