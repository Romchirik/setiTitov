package nsu.titov.engine;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.Request;
import org.asynchttpclient.Response;

import java.util.function.Consumer;

import static org.asynchttpclient.Dsl.asyncHttpClient;
import static org.asynchttpclient.Dsl.get;

public class HttpServer {
    private static HttpServer instance = null;

    private final Logger logger = LogManager.getLogger(getClass());
    private final AsyncHttpClient httpClient = asyncHttpClient();

    private static HttpServer getInstance() {
        if (instance == null) {

            instance = new HttpServer();
            instance.logger.info("Started http server");
        }
        return instance;
    }


    public static synchronized void sendGetRequest(String url,
                                                   Consumer<Response> onSuccess,
                                                   Consumer<Response> onError) {
        var inst = getInstance();
        Request request = get(url).build();
        inst.logger.debug(String.format("Requesting from %s", url));
        inst.httpClient
                .executeRequest(request)
                .toCompletableFuture()
                .thenApply((response -> {
                    inst.logger.debug(String.format("Request finished status code: %d", response.getStatusCode()));
                    if (200 == response.getStatusCode()) {
                        onSuccess.accept(response);
                    } else {
                        onError.accept(response);
                    }
                    return response;
                }));
    }
}


