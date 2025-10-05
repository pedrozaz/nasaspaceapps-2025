package github.pedrozaz.meteormadness.dto.nasa;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data @JsonIgnoreProperties(ignoreUnknown = true)
public class FeedDTO {
    @JsonProperty("id") private String id;
    @JsonProperty("name") private String name;
    @JsonProperty("is_potentially_hazardous_asteroid" ) private boolean isPotentiallyHazardous;
}


