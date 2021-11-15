package nsu.titov.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenTripInfoResponse {
    public String name;
    public Info info;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public class Info {
        @JsonProperty("descr")
        public String description;
    }
}
