package nsu.titov.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GraphHopperResponse {
    @JsonProperty("hits")
    public ArrayList<GraphHopperItem> items;
}

