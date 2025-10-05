package github.pedrozaz.meteormadness.service;

import github.pedrozaz.meteormadness.dto.nasa.AsteroidDTO;
import github.pedrozaz.meteormadness.dto.nasa.FeedDTO;
import github.pedrozaz.meteormadness.dto.response.FeedResponseDTO;
import github.pedrozaz.meteormadness.util.DateRange;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cglib.core.Local;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service @Slf4j
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

    public Mono<FeedResponseDTO> getAsteroidsForDateRange(LocalDate startDate, LocalDate endDate) {
        return this.webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/feed")
                        .queryParam("start_date", startDate.toString())
                        .queryParam("end_date", endDate.toString())
                        .queryParam("api_key", this.key)
                        .build())
                .retrieve()
                .bodyToMono(FeedResponseDTO.class);
    }

    public Flux<FeedDTO> getAllAsteroidsForLongDateRange(LocalDate startDate, LocalDate endDate) {
        List<DateRange> dateRanges = generateDateRanges(startDate, endDate);

        return Flux.fromIterable(dateRanges)
                .flatMap(range -> {
                    log.info("Fetching for range {} to {}", range.startDate(), range.endDate());
                    return getAsteroidsForDateRange(range.startDate(), range.endDate());
                })
                .flatMap(feedResponse -> Flux.fromIterable(
                        feedResponse.getNearEarthObjects().values().stream()
                                .flatMap(List::stream)
                                .toList()
                ));
    }

    private List<DateRange> generateDateRanges(LocalDate startDate, LocalDate endDate) {
        List<DateRange> dateRanges = new ArrayList<>();
        LocalDate currentStart = startDate;
        while(currentStart.isBefore(endDate) ||  currentStart.equals(endDate)) {
            LocalDate currentEnd = currentStart.plusDays(6);
            if (currentEnd.isAfter(endDate)) {
                currentEnd = endDate;
            }
            dateRanges.add(new DateRange(currentStart, currentEnd));
            currentStart = currentStart.plusDays(1);
        }
        return  dateRanges;
    }
}
