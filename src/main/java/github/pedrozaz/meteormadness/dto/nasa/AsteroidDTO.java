package github.pedrozaz.meteormadness.dto.nasa;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data @JsonIgnoreProperties(ignoreUnknown = true)
public class AsteroidDTO {
    @JsonProperty("id") private String id;
    @JsonProperty("name") private String name;
    @JsonProperty("is_potentially_hazardous_asteroid") private boolean isPotentiallyHazardousAsteroid;
    @JsonProperty("estimated_diameter") private Map<String, DiameterDTO> estimatedDiameter;
    @JsonProperty("close_approach_data") private List<CloseApproachDataDTO> closeApproachData;
    @JsonProperty("orbital_data") private OrbitalDataDTO orbitalData;
}
