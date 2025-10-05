package github.pedrozaz.meteormadness.service;

import github.pedrozaz.meteormadness.dto.nasa.AsteroidDTO;
import github.pedrozaz.meteormadness.dto.nasa.CloseApproachDataDTO;
import github.pedrozaz.meteormadness.dto.nasa.FeedDTO;
import github.pedrozaz.meteormadness.dto.nasa.OrbitalDataDTO;
import github.pedrozaz.meteormadness.dto.response.ImpactDTO;
import github.pedrozaz.meteormadness.dto.response.PositionDTO;
import github.pedrozaz.meteormadness.dto.response.SimulationResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service @Slf4j
public class EngineService {

    private static final double ASTEROID_DENSITY_KG_M3 = 3000;
    private static final double JOULES_PER_MEGATON_TNT = 4.184e15;
    private final NeoApiService neoApiService;

    public EngineService(NeoApiService neoApiService) {
        this.neoApiService = neoApiService;
    }

    public List<PositionDTO> generateOrbitalTrajectory(OrbitalDataDTO orbitalDataDTO) {
        List<PositionDTO> trajectory = new ArrayList<>();
        int steps = 360;

        double eccentricity = orbitalDataDTO.getEccentricity();
        double semiMajorAxis = orbitalDataDTO.getSemiMajorAxis();
        double inclination = Math.toRadians(orbitalDataDTO.getInclination());
        double ascNodeLongitude = Math.toRadians(orbitalDataDTO.getAscendingNodeLongitude());
        double perihelionArgument = Math.toRadians(orbitalDataDTO.getPerihelionArgument());

        double cosInclination = Math.cos(inclination);
        double sinInclination = Math.sin(inclination);
        double cosAscNode = Math.cos(ascNodeLongitude);
        double sinAscNode = Math.sin(ascNodeLongitude);
        double cosPerihelion =  Math.cos(perihelionArgument);
        double sinPerihelion = Math.sin(perihelionArgument);

        for (int i = 0; i < steps; i++) {
            double meanAnomaly = Math.toRadians(i);
            double eccentricityAnomaly = solveKepler(meanAnomaly, eccentricity);
            double xOrbitalPlane = semiMajorAxis * (Math.cos(eccentricityAnomaly) - eccentricity);
            double yOrbitalPlane = semiMajorAxis * Math.sqrt(1 - eccentricity * eccentricity) *
                    Math.sin(eccentricityAnomaly);

            double x = xOrbitalPlane * (cosPerihelion * cosAscNode - sinPerihelion * sinAscNode * cosInclination)
                    - yOrbitalPlane * (sinPerihelion * cosAscNode + cosPerihelion * sinAscNode * cosInclination);

            double y = xOrbitalPlane * (cosPerihelion * sinAscNode + sinPerihelion * cosAscNode * cosInclination)
                    + yOrbitalPlane * (-sinPerihelion * sinAscNode + cosPerihelion * cosAscNode * cosInclination);

            double z = xOrbitalPlane * (sinPerihelion * sinInclination)
                    + yOrbitalPlane * (cosPerihelion * sinInclination);

            trajectory.add(new PositionDTO(x, y, z));
        }
        log.info("Generated trajectory with {} points for semi-major axis {}", trajectory.size(), semiMajorAxis);
        return trajectory;
    }

    public ImpactDTO analyzeImpact(AsteroidDTO asteroid, double distanceLimitKM) {
        if (asteroid.getCloseApproachData() == null || asteroid.getCloseApproachData().isEmpty()) {
            return ImpactDTO.builder().hasImpactRisk(false).build();
        }
        CloseApproachDataDTO closestEarthApproach = asteroid.getCloseApproachData().stream()
                .filter(cad -> "Earth".equalsIgnoreCase(cad.getOrbitingBody()))
                .min(Comparator.comparing(cad -> Double.parseDouble(cad.getMissDistance().get("kilometers"))))
                .orElse(null);

        if (closestEarthApproach == null) {
            log.info("No close approach data found for Earth for asteroid {}", asteroid.getId());
            return ImpactDTO.builder().hasImpactRisk(false).build();
        }

        double minDistance = Double.parseDouble(closestEarthApproach.getMissDistance().get("kilometers"));
        boolean isPotentiallyHazardous = asteroid.isPotentiallyHazardousAsteroid();
        double averageDiameterMeters = (asteroid.getEstimatedDiameter().get("meters").getMin() + asteroid.getEstimatedDiameter().get("meters").getMax()) / 2.0;
        double velocityKMS = Double.parseDouble(closestEarthApproach.getRelativeVelocity().get("kilometers_per_second"));
        double megatons = calculateImpactEnergy(averageDiameterMeters, velocityKMS);
        double blastRadius = calculateBlastRadius(megatons);

        if (minDistance <= distanceLimitKM) {
            log.warn("IMPACT RISK DETECTED for asteroid {}! Distance: {} km, Energy: {} Megatons", asteroid.getId(), minDistance, megatons);

            return ImpactDTO.builder()
                    .hasImpactRisk(true)
                    .closestApproachDate(closestEarthApproach.getCloseApproachDate())
                    .minDistanceMeters(minDistance * 1000)
                    .impactMetersPerSecond(velocityKMS * 1000)
                    .impactMegatons(megatons)
                    .isPotentiallyHazardous(isPotentiallyHazardous)
                    .estimatedDiameterMeters(averageDiameterMeters)
                    .blastRadiusKm(blastRadius)
                    .build();
        }

        log.info("No impact risk for asteroid {}. Closest approach to Earth: {} km", asteroid.getId(), minDistance);
        return ImpactDTO.builder()
                .hasImpactRisk(false)
                .closestApproachDate(closestEarthApproach.getCloseApproachDate())
                .minDistanceMeters(minDistance * 1000)
                .isPotentiallyHazardous(isPotentiallyHazardous)
                .estimatedDiameterMeters(averageDiameterMeters)
                .build();
    }

    public Flux<SimulationResponseDTO> analyzeHazardousForDateRange(LocalDate startDate, LocalDate endDate) {
        return neoApiService.getAllAsteroidsForLongDateRange(startDate, endDate)
                .filter(FeedDTO::isPotentiallyHazardous)
                .map(FeedDTO::getId)
                .distinct()
                .parallel()
                .runOn(Schedulers.parallel())
                .flatMap(neoApiService::getAsteroid)
                .sequential()
                .map(asteroid -> {
                    log.info("Processing hazardous asteroid: {}", asteroid.getName());
                    List<PositionDTO> trajectory = generateOrbitalTrajectory(asteroid.getOrbitalData());
                    ImpactDTO impact = analyzeImpact(asteroid, 7500000);
                    return new SimulationResponseDTO(asteroid.getId(), asteroid.getName() , trajectory, impact);
                });
    }

    private double calculateImpactEnergy(double diameterMeters, double velocity) {
        double radius = diameterMeters / 2;
        double volume = (4.0/3.0) * Math.PI * Math.pow(radius, 3);
        double mass = volume * ASTEROID_DENSITY_KG_M3;

        double velocityMs = velocity * 1000;
        double energyJoules = 0.5 * mass * Math.pow(velocityMs, 2);

        return energyJoules / JOULES_PER_MEGATON_TNT;
    }

    private double solveKepler(double meanAnomalyM, double eccentricityE) {
        double E = meanAnomalyM;
        for (int i = 0; i < 10; i++) {
            double delta = (E - eccentricityE * Math.sin(E) - meanAnomalyM) / (1 - eccentricityE * Math.cos(E));
            E -= delta;
        }
        return E;
    }

    private double calculateBlastRadius(double megatons) {
        if (megatons <- 0) {
            return 0;
        }
        return 1.1 * Math.pow(megatons, 1.0/3.0);
    }
}