package github.pedrozaz.meteormadness.controller;

import github.pedrozaz.meteormadness.dto.nasa.AsteroidDTO;
import github.pedrozaz.meteormadness.dto.response.ImpactDTO;
import github.pedrozaz.meteormadness.dto.response.PositionDTO;
import github.pedrozaz.meteormadness.dto.response.SimulationResponseDTO;
import github.pedrozaz.meteormadness.service.EngineService;
import github.pedrozaz.meteormadness.service.NeoApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;

@RestController @RequestMapping("/api/simulation")
public class SimulationController {

    @Autowired private NeoApiService neoApiService;
    @Autowired private EngineService engineService;

    @GetMapping("/{id}")
    public Mono<SimulationResponseDTO> getSimulation(
            @PathVariable String id,
            @RequestParam(defaultValue = "10000000") double impactLimitKm) {

        return neoApiService.getAsteroid(id)
                .map(asteroid -> {
                    List<PositionDTO> trajectory = engineService.generateOrbitalTrajectory(asteroid.getOrbitalData());
                    ImpactDTO impact = engineService.analyzeImpact(asteroid, impactLimitKm);
                    return new SimulationResponseDTO(asteroid.getId(), asteroid.getName(), trajectory, impact);
                });
    }

    @GetMapping("/feed")
    public Flux<SimulationResponseDTO> getHazardousByRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return engineService.analyzeHazardousForDateRange(startDate, endDate);
    }

    @GetMapping("/debug/{id}")
    public Mono<AsteroidDTO> debugSimulation(@PathVariable String id) {
        return neoApiService.getAsteroid(id);
    }
}
