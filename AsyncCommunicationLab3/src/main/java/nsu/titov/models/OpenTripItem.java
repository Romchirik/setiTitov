package nsu.titov.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenTripItem {
    public String xid;
    public String name;
    public String wikidata;
    public double dist;
    Point point;

    @Override
    public String toString() {
        return String.format("%s | distance from selected: %.1f km",
                "".equals(name) || name == null ? "No name loaded for this place" : name,
                dist / 1000.0);
    }
}
