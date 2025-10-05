package github.pedrozaz.meteormadness.service;

import github.pedrozaz.meteormadness.dto.nasa.AsteroidDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class NeoApiService {

    @Autowired
    private WebClient webClient;

    @Value("${neows.api.key}")
    private String key;

    public NeoApiService(WebClient.Builder webClientBuilder, @Value("${neows.api.baseurl}") String baseUrl) {
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
    }

    public Mono<AsteroidDTO> getAsteroid(String id) {
        return this.webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/neo/{id}")
                        .queryParam("api_key", this.key)
                        .build(id))
                .retrieve()
                .bodyToMono(AsteroidDTO.class);
    }
}
