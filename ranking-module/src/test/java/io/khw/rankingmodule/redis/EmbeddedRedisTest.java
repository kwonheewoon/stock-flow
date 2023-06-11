package io.khw.rankingmodule.redis;


import io.khw.rankingmodule.RankingModuleApplication;
import io.khw.rankingmodule.config.TestRedisConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveZSetOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Flux;
import redis.embedded.RedisServer;

import java.io.IOException;

@SpringBootTest(classes = {RankingModuleApplication.class, TestRedisConfiguration.class},webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class EmbeddedRedisTest {

    @Autowired
    ReactiveRedisTemplate<String, String> reactiveRedisTemplate;

    @Test
    public void testEmbeddedRedis() {
        // Embedded Redis 사용 예시
        ReactiveZSetOperations<String, String> zSetOperations = reactiveRedisTemplate.opsForZSet();

        zSetOperations.add("stock", "삼성전자", 100)
                .then(zSetOperations.add("stock", "삼성물산", 200))
                .then(zSetOperations.add("stock", "삼성전자", 300))
                .then(zSetOperations.add("stock", "삼성전자", 290))
                .block(); // block()은 테스트 용도. 실제 코드에서는 사용하지 않는다.

        Flux<ZSetOperations.TypedTuple<String>> df = zSetOperations.reverseRangeByScoreWithScores("stock", Range.closed(0D, 500D));

        df.doOnNext(tuple -> {
            System.out.println("데이터: " + tuple.getValue() + ", 점수: " + tuple.getScore());
        }).blockLast(); // blockLast()는 테스트 용도. 실제 코드에서는 사용하지 않는다.
    }
}
