package github.pedrozaz.meteormadness.dto.nasa;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data @JsonIgnoreProperties(ignoreUnknown = true)
public class DiameterDTO {
    @JsonProperty("estimated_diameter_min") private double min;
    @JsonProperty("estimated_diameter_max") private double max;
}
