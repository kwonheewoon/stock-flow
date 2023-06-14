package io.khw.ranking.redis;


import io.khw.ranking.RankingModuleApplication;
import io.khw.ranking.config.TestRedisConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveZSetOperations;
import org.springframework.data.redis.core.ZSetOperations;
import reactor.core.publisher.Flux;

@SpringBootTest(classes = {RankingModuleApplication.class, TestRedisConfiguration.class},webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class EmbeddedRedisTest {

    @Autowired
    ReactiveRedisTemplate<String, String> reactiveRedisTemplate;

    @Test
    public void rankingVolume() {
        // Embedded Redis 사용 예시
        ReactiveZSetOperations<String, String> zSetOperations = reactiveRedisTemplate.opsForZSet();

        zSetOperations.add("stock:ranking:volume", "삼성전자", 6500)
                .then(zSetOperations.add("stock:ranking:volume", "카카오", 7549))
                .then(zSetOperations.add("stock:ranking:volume", "카카오페이", 2855))
                .then(zSetOperations.add("stock:ranking:volume", "카카오뱅크", 1140))
                .block(); // block()은 테스트 용도. 실제 코드에서는 사용하지 않는다.

        Flux<ZSetOperations.TypedTuple<String>> df = zSetOperations.reverseRangeWithScores("stock:ranking:volume", Range.closed(0L, 500L));

        df.doOnNext(tuple -> {
            System.out.println("데이터: " + tuple.getValue() + ", 점수: " + tuple.getScore());
        }).blockLast(); // blockLast()는 테스트 용도. 실제 코드에서는 사용하지 않는다.
    }

    @Test
    public void testEmbeddedRedis() {
        // Embedded Redis 사용 예시
        ReactiveZSetOperations<String, String> zSetOperations = reactiveRedisTemplate.opsForZSet();

        zSetOperations.add("stock", "삼성전자", 100)
                .then(zSetOperations.add("stock", "삼성물산", 200))
                .then(zSetOperations.add("stock", "삼성전자", 300))
                .then(zSetOperations.add("stock", "삼성전자", 290))
                .then(zSetOperations.add("stock", "카카오페이", -500))
                .then(zSetOperations.add("stock", "카카오뱅크", -200))
                .block(); // block()은 테스트 용도. 실제 코드에서는 사용하지 않는다.

        Flux<ZSetOperations.TypedTuple<String>> df = zSetOperations.reverseRangeWithScores("stock", Range.closed(0L, 10L));

        df.doOnNext(tuple -> {
            System.out.println("데이터: " + tuple.getValue() + ", 점수: " + tuple.getScore());
        }).blockLast(); // blockLast()는 테스트 용도. 실제 코드에서는 사용하지 않는다.
    }
}
