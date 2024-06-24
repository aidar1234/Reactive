package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@RestController
@RequiredArgsConstructor
public class TestController {

    private final RestTemplate restTemplate;
    private final WebClient webClient;

    @GetMapping("/sync")
    public String getStringSync() {
        System.out.println("REQUEST");

        return restTemplate.getForObject("http://localhost:8082/get", String.class);
    }

    @GetMapping("/reactive") //Если размер пула потоков tomcat ограничен, будут обработаны всё равно все запросы
    public Mono<String> getStringReactive() {
        System.out.println("REQUEST");

        return webClient.get()
                .uri("http://localhost:8082/get")
                .retrieve()
                .bodyToMono(String.class)
                .subscribeOn(Schedulers.boundedElastic());
    }
}
