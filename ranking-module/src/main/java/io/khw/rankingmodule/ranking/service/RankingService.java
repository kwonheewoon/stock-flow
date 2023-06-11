package io.khw.rankingmodule.ranking.service;

import io.khw.common.constants.ApiResponseCode;
import io.khw.common.exeception.RankingException;
import io.khw.common.response.CommonResponse;
import io.khw.common.response.ErrCommonResponse;
import io.khw.domain.stock.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveZSetOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class RankingService {

    private final StockRepository stockRepository;

    private final ReactiveRedisTemplate<String, String> reactiveRedisTemplate;


    public Mono<Double> increasePopularityRank(Long stockId, String stockCode) {
        ReactiveZSetOperations<String, String> zSetOperations = reactiveRedisTemplate.opsForZSet();

        return stockRepository.findByIdAndCode(stockId, stockCode)
                .switchIfEmpty(Mono.error(new RankingException(ApiResponseCode.STOCK_NOT_FOUND))) // 주식이 존재하지 않을 경우 예외 발생
                //.then(zSetOperations.incrementScore("stock:popularity:volume", stockId + ":" + stockCode, 1))
                .then(Mono.defer(() -> zSetOperations.incrementScore("stock:popularity:volume", stockId + ":" + stockCode, 1)))
                .doOnError(e -> {
                    log.error("Failed to increment popularity rank for stock: " + stockCode, e);
                })
                .onErrorResume(e -> Mono.error(new RankingException(ApiResponseCode.REDIS_SERVER_ERROR)));
    }

    public Mono<Double> increaseVolumeRank(Long stockId, String stockCode, double tradeVolume) {
        ReactiveZSetOperations<String, String> zSetOperations = reactiveRedisTemplate.opsForZSet();

        return zSetOperations.incrementScore("stock:ranking:volume", stockId + ":" + stockCode, tradeVolume)
                .doOnError(e -> {
                    log.error("Failed to increment volume rank for stock: " + stockCode, e);
                })
                .onErrorResume(e -> Mono.error(new RankingException(ApiResponseCode.REDIS_SERVER_ERROR)));
    }

}
