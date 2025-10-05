package github.pedrozaz.meteormadness.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import github.pedrozaz.meteormadness.dto.nasa.FeedDTO;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data @JsonIgnoreProperties(ignoreUnknown = true)
public class FeedResponseDTO {
    @JsonProperty("near_earth_objects") private Map<String, List<FeedDTO>> nearEarthObjects;
}
