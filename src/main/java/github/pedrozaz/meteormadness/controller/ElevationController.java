package github.pedrozaz.meteormadness.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.Random;

@RestController
@RequestMapping("/api/elevation")
public class ElevationController {

    @GetMapping
    public ResponseEntity<?> getElevation(@RequestParam double lat, @RequestParam double lon) {
        // 🔹 Mock temporário: gera uma elevação aleatória entre 0 e 3000 metros
        double elevation = new Random().nextDouble() * 3000;

        // 🔹 Monta resposta
        var response = new ElevationResponse(lat, lon, elevation, "meters");

        return ResponseEntity.ok(response);
    }

    // Classe interna simples para modelar o retorno JSON
    public record ElevationResponse(double latitude, double longitude, double elevation, String units) {}
}
