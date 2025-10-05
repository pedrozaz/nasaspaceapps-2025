package github.pedrozaz.meteormadness.dto.nasa;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

@Data @JsonIgnoreProperties(ignoreUnknown = true)
public class CloseApproachDataDTO {
    @JsonProperty("close_approach_date") private String closeApproachDate;
    @JsonProperty("relative_velocity") private Map<String, String> relativeVelocity;
    @JsonProperty("miss_distance") private Map<String, String> missDistance;
}
