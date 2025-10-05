package github.pedrozaz.meteormadness.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PositionDTO {
    private double x;
    private double y;
    private double z;
}
