package nsu.titov.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GraphHopperItem {
    public Point point;
    public String name;
    public String country;
    @JsonProperty("countrycode")
    public String countryCode;
    public String city;

    @JsonIgnore
    public String toString() {
        var builder = new StringBuilder();
        if (country != null) {
            builder.append(String.format("%s | %s", countryCode, city == null ? "\n" : ""));
        }
        if (city != null) {
            builder.append(String.format("city: %s\n", city));
        }
        builder.append(String.format("place: %s", name));

        return builder.toString();
    }
}