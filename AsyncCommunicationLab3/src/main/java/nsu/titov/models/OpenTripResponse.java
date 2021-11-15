package nsu.titov.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenTripResponse extends ArrayList<OpenTripItem> {
}
