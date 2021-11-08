package nsu.titov.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenWeatherResponse {
    public Main main;
    public Wind wind;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public class Wind {
        public double speed;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public class Main {
        public double temp;
        @JsonProperty("feels_like")
        public double feelsLike;
    }
}
