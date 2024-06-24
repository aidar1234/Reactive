package org.example;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class Main2 {

    public static void main(String[] args) throws InterruptedException {
//        Flux<Integer> flux = Flux.generate(
//                () -> 1,
//                (state, sink) -> {
//                    if (state == 3) {
//                        try {
//                            Thread.sleep(5000L);
//                        } catch (InterruptedException e) {
//                            throw new RuntimeException(e);
//                        }
//                    }
//                    sink.next(state);
//                    if (state == 5) {
//                        sink.complete();
//                    }
//                    return ++state;
//                }
//        );

        Mono<Integer> mono1 = Mono.fromCallable(() -> 1);
        Mono<Integer> mono2 = Mono.fromCallable(() -> {
            Thread.sleep(3000);
            return 2;
        });
        Mono<Integer> mono3 = Mono.fromCallable(() -> 3);

        Flux<Integer> flux = Flux.merge(mono1, mono2, mono3);

        flux
                .doOnNext(System.out::println)
                .log()
                .subscribe();

//        List<Mono<Integer>> monos = List.of(mono1, mono2, mono3);
//        for (Mono<Integer> mono : monos) {
//            mono
//                    .subscribeOn(Schedulers.boundedElastic())
//                    .subscribe(System.out::println); // передает callback
//        }

        Thread.sleep(50000L);
    }
}
