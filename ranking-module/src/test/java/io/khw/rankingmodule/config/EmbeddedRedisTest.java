package io.khw.rankingmodule.config;


import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import redis.embedded.RedisServer;

import java.io.IOException;

//@ActiveProfiles("local")
@SpringBootTest(classes = TestRedisConfiguration.class)
public class EmbeddedRedisTest {

    @Autowired
    RedisTemplate<String, String> redisTemplate;

    @Test
    public void testEmbeddedRedis() {
        // Embedded Redis 사용 예시
        ZSetOperations<String, String> zSetOperations = redisTemplate.opsForZSet();
        zSetOperations.add("stock", "삼성전자", 100);
        zSetOperations.add("stock", "삼성물산", 200);

        var df = zSetOperations.rangeByScoreWithScores("stock", 0, 500);

        System.out.println("데이터 : " + df);
    }
}
