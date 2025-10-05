package github.pedrozaz.meteormadness.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data @AllArgsConstructor
public class SimulationResponseDTO {
    String id;
    String name;
    private List<PositionDTO> orbitalTrajectory;
    private ImpactDTO impactData;
}
