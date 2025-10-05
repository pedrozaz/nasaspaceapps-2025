package github.pedrozaz.meteormadness.dto.response;

import lombok.Builder;
import lombok.Data;

@Data @Builder
public class ImpactDTO {
    private boolean hasImpactRisk;
    private String closestApproachDate;
    private double minDistanceMeters;
    private double impactMetersPerSecond;
    private double impactMegatons;
    private boolean isPotentiallyHazardous;
    private double estimatedDiameterMeters;
}
