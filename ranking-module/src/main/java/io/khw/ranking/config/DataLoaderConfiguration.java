package io.khw.ranking.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.khw.common.constants.Const;
import io.khw.domain.stock.repository.StockRepository;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.ReactiveHashOperations;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class DataLoaderConfiguration {

    private final StockRepository stockRepository;

    @Bean
    public CommandLineRunner loadData(ReactiveRedisTemplate<String, String> reactiveRedisTemplate) {
        return args -> {
            ObjectMapper objectMapper = new ObjectMapper();


            stockRepository.findAll().flatMap(stockEntity -> {
                Map<String, String> savedValueMap = Map.of("name" , stockEntity.getName(), "currentPrice", stockEntity.getPrice().toString(), "percent", "0.00");
                String savedValueJson;
                try {
                    savedValueJson = objectMapper.writeValueAsString(savedValueMap);
                } catch (JsonProcessingException e) {
                    return Mono.error(e);
                }

                Mono<Boolean> hashOp = reactiveRedisTemplate.opsForHash()
                        .put(Const.STOCK_PRICE_KEY, stockEntity.getId()+":"+stockEntity.getCode(), savedValueJson);

                Mono<Double> zSetOp1 = reactiveRedisTemplate.opsForZSet().incrementScore(Const.STOCK_RANKING_VOLUME_KEY, stockEntity.getId() + ":" + stockEntity.getCode(), 0);
                Mono<Double> zSetOp2 = reactiveRedisTemplate.opsForZSet().incrementScore(Const.STOCK_RANKING_POPULARITY_KEY, stockEntity.getId() + ":" + stockEntity.getCode(), 0);
                Mono<Double> zSetOp3 = reactiveRedisTemplate.opsForZSet().incrementScore(Const.STOCK_INC_PRICE_DELTA_KEY, stockEntity.getId() + ":" + stockEntity.getCode(), 0);
                Mono<Double> zSetOp4 = reactiveRedisTemplate.opsForZSet().incrementScore(Const.STOCK_DEC_PRICE_DELTA_KEY, stockEntity.getId() + ":" + stockEntity.getCode(), 0);

                return Mono.zip(hashOp, zSetOp1, zSetOp2, zSetOp3, zSetOp4)
                        .thenReturn(true);
            }).subscribe();
        };
    }
}
