package github.pedrozaz.meteormadness.dto.nasa;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data @JsonIgnoreProperties(ignoreUnknown = true)
public class OrbitalDataDTO {
    @JsonProperty("eccentricity") private double eccentricity;
    @JsonProperty("semi_major_axis") private double semiMajorAxis;
    @JsonProperty("inclination") private double inclination;
    @JsonProperty("ascending_node_longitude") private double ascendingNodeLongitude;
    @JsonProperty("perihelion_argument") private double perihelionArgument;
    @JsonProperty("mean_anomaly") private double meanAnomaly;
    @JsonProperty("epoch_osculation") private double epochOsculation;
}
