package org.example;

import lombok.SneakyThrows;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Main {

    @SneakyThrows
    public static void main(String[] args) {
        RestTemplate restTemplate = new RestTemplate();
        WebClient webClient = WebClient.builder().build();

        final int POOL_SIZE = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(POOL_SIZE);

        Collection<Callable<Object>> callables = new ArrayList<>();
        Queue<Mono<String>> monos = new ConcurrentLinkedQueue<>();

        // Чтобы проверить блокирующую версию, надо закомментировать вызов по webClient и раскомментировать вызов по restTemplate
        for (int i = 0; i < 40; i++) { // Размер пула потоков = 10, запросов = 40
            callables.add(() -> {
                Mono<String> mono = webClient.get() // не выполняется здесь, ждет подписки на publisher
                        .uri("http://localhost:8081/reactive")
                        .retrieve()
                        .bodyToMono(String.class)
                        .log(); // <- вспомогательный метод, логирующий вызовы

                monos.add(mono);

//                String result = restTemplate.getForObject("http://localhost:8081/sync", String.class); // выполняется сразу
//                System.out.println(result);
                return null;
            });
        }

        long start = System.currentTimeMillis();

        List<Future<Object>> futures = executorService.invokeAll(callables);
        for (Future<Object> future : futures) {
            future.get(); // В случае с webClient вызов get() не блокируется
        }

        Flux<String> flux = Flux.merge(monos);
        // Можно было не делать Flux.merge(), а вызывать метод block() у каждого Mono в списке

        flux
                .doOnNext(System.out::println)
                .blockLast(); // Подписка (терминальная операция) - запускает publisher (flux),
                             // В данном случае это блокирующая операция, будет ждать окончания последнего запроса,
                            // выполняя все запросы (подписываясь на каждый Mono) последователльно
                           // Если в середине какой-то запрос будет долгий, то все последующие будут вынуждены ждать его
                          // Чтобы избежать этого надо ЛИБО прописать определенную логику в методе .subscribe(new Consumer())
                         // Предварительно вызвав .subscribeOn(Schedulers.boundedElastic() или Schedulers.parallel()) на каждом Mono (за нас это уже делает webClient)
                        // чтобы подписка на Mono была в отдельном потоке
                       // и не блокировала основной поток
                      // По умолчанию, число вспомогательных потоков в Schedulers.boundedElastic() = кол-во логических ядер процессора, и задачи оптимально распределяются между ними
                     // ЛИБО возварщать из метода объект Mono или Flux

        long end = System.currentTimeMillis();
        System.out.println("Finish - " + (end - start) + " milliseconds");

//        executorService.shutdown();
    }
}
