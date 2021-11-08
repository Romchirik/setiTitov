package nsu.titov.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Point {

    @JsonCreator
    public Point(
            @JsonProperty("lat")
                    double lat,
            @JsonProperty("lng")
                    double lon
    ) {
        this.lat = lat;
        this.lon = lon;
    }

    public double lat;
    public double lon;
}
