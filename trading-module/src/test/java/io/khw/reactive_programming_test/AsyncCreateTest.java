package io.khw.reactive_programming_test;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;

@Slf4j
public class AsyncCreateTest {
    int start = 1;
    int end = 4;

    @Test
    void 테스팅() throws InterruptedException {
        Flux.create((FluxSink<Integer> sink) -> {
            sink.onRequest(n -> {
                log.info("# requested: " + n);
                try {
                    Thread.sleep(500L);
                    for(int i = start;i<=end;i++){
                        sink.next(i);
                    }
                    start += 4;
                    end += 4;
                } catch (InterruptedException e){

                }
            });

            sink.onDispose(() -> {
                log.info("# clean up");
            });
        }, FluxSink.OverflowStrategy.DROP)
                .subscribeOn(Schedulers.boundedElastic())
                .publishOn(Schedulers.parallel(), 2)
                .subscribe(data -> log.info("# onNext : {}", data));
        Thread.sleep(3000L);
    }

    @Test
    void flatmap테스팅() throws InterruptedException {
        Flux.range(2,8)
                .flatMap(dan -> Flux.range(1,9)
                        .publishOn(Schedulers.parallel())//오퍼레이터 비동기 처리
                        .map(n -> dan + " * " + n + " = " + dan * n))
                .subscribe(log::info);

        Thread.sleep(100L);
    }

    @Test
    void merge테스팅() throws InterruptedException {
        //각각의 시퀀스에서 emit을 기다리는게 아니라 emit되 시퀀스부터 구독함
        Flux.merge(
                Flux.just(1,2,3,4).delayElements(Duration.ofMillis(300L)),
                Flux.just(5,6,7,8).delayElements(Duration.ofMillis(300L))
        )
                .subscribe(data -> log.info("# onNext : {}",data));

        Thread.sleep(2000L);
    }

    @Test
    void zip테스팅() throws InterruptedException {
        //zip는 각각의 시퀀스에서 emit을 기다렸다가 구독함
        Flux.zip(
                        Flux.just(1,2,3).delayElements(Duration.ofMillis(300L)),
                        Flux.just(4,5,6).delayElements(Duration.ofMillis(500L)),
                        (n1,n2) -> n1*n2
                )
                .subscribe(tuple2 -> log.info("# onNext : {}",tuple2));

        Thread.sleep(2500L);
    }

    @Test
    void zip테스팅2() throws InterruptedException {

    }


}
