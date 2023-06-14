package io.khw.ranking.ranking.service;

import io.khw.common.constants.ApiResponseCode;
import io.khw.common.constants.Const;
import io.khw.common.exeception.RankingException;
import io.khw.domain.common.vo.SearchVo;
import io.khw.domain.stock.converter.StockConverter;
import io.khw.domain.stock.dto.StockPriceDeltaRankApiDto;
import io.khw.domain.stock.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveZSetOperations;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Slf4j
@Service
@RequiredArgsConstructor
public class RankingService {

    private final StockRepository stockRepository;

    private final StockConverter stockConverter;

    private final ReactiveRedisTemplate<String, String> reactiveRedisTemplate;


    public Mono<Double> increaseVolumeRank(Long stockId, String stockCode, double tradeVolume) {
        ReactiveZSetOperations<String, String> zSetOperations = reactiveRedisTemplate.opsForZSet();

        return stockRepository.findByIdAndCode(stockId, stockCode)
                .switchIfEmpty(Mono.error(new RankingException(ApiResponseCode.STOCK_NOT_FOUND))) // 주식이 존재하지 않을 경우 예외 발생
                .then(Mono.defer(() -> zSetOperations.incrementScore(Const.STOCK_RANKING_VOLUME_KEY, stockId + ":" + stockCode, tradeVolume)))
                .doOnError(e -> {
                    log.error("Failed to increment volume rank for stock: " + stockCode, e);
                })
                .onErrorResume(e -> Mono.error(e));
    }

    public Flux<StockPriceDeltaRankApiDto> findAllStocksVolumeRanking(SearchVo searchVo) {
        ReactiveZSetOperations<String, String> zSetOperations = reactiveRedisTemplate.opsForZSet();

        // get the range from sorted set in ascending order
        return zSetOperations.reverseRangeWithScores(Const.STOCK_RANKING_VOLUME_KEY, Range.closed(searchVo.getStartIndex(), searchVo.getEndIndex()))
                .flatMap(tuple -> {
                    String[] split = tuple.getValue().split(":");
                    Long stockId = Long.valueOf(split[0]);
                    String stockCode = split[1];

                    return stockRepository.findByIdAndCode(stockId, stockCode)
                            .switchIfEmpty(Mono.error(new RankingException(ApiResponseCode.STOCK_NOT_FOUND)))
                            .flatMap(stockEntity -> findStockPriceDeltaByStockIdAndCode(stockId, stockCode)
                                    .map(priceDelta -> {
                                        var stockPriceDeltaRankApiDto = stockConverter.toStockPriceDeltaRankApiDto(stockEntity);
                                        stockPriceDeltaRankApiDto.setPriceDeltaPercentage(priceDelta);
                                        return stockPriceDeltaRankApiDto;
                                    }));
                })
                .onErrorMap(NumberFormatException.class, e -> new RankingException(ApiResponseCode.SERVER_ERROR))
                .onErrorMap(NullPointerException.class, e -> new RankingException(ApiResponseCode.SERVER_ERROR));
    }

    public Mono<Double> increasePopularityRank(Long stockId, String stockCode) {
        ReactiveZSetOperations<String, String> zSetOperations = reactiveRedisTemplate.opsForZSet();

        return stockRepository.findByIdAndCode(stockId, stockCode)
                .switchIfEmpty(Mono.error(new RankingException(ApiResponseCode.STOCK_NOT_FOUND))) // 주식이 존재하지 않을 경우 예외 발생
                .then(Mono.defer(() -> zSetOperations.incrementScore(Const.STOCK_RANKING_POPULARITY_KEY, stockId + ":" + stockCode, 1)))
                .doOnError(e -> {
                    log.error("Failed to increment popularity rank for stock: " + stockCode, e);
                })
                .onErrorResume(e -> Mono.error(e));
    }

    public Flux<StockPriceDeltaRankApiDto> findAllStocksPopularityRanking(SearchVo searchVo) {
        ReactiveZSetOperations<String, String> zSetOperations = reactiveRedisTemplate.opsForZSet();

        // get the range from sorted set in ascending order
        return zSetOperations.reverseRangeWithScores(Const.STOCK_RANKING_POPULARITY_KEY, Range.closed(searchVo.getStartIndex(), searchVo.getEndIndex()))
                .flatMap(tuple -> {
                    String[] split = tuple.getValue().split(":");
                    Long stockId = Long.valueOf(split[0]);
                    String stockCode = split[1];

                    return stockRepository.findByIdAndCode(stockId, stockCode)
                            .switchIfEmpty(Mono.error(new RankingException(ApiResponseCode.STOCK_NOT_FOUND)))
                            .flatMap(stockEntity -> findStockPriceDeltaByStockIdAndCode(stockId, stockCode)
                                    .map(priceDelta -> {
                                        var stockPriceDeltaRankApiDto = stockConverter.toStockPriceDeltaRankApiDto(stockEntity);
                                        stockPriceDeltaRankApiDto.setPriceDeltaPercentage(priceDelta);
                                        return stockPriceDeltaRankApiDto;
                                    }));
                }).onErrorMap(NumberFormatException.class, e -> new RankingException(ApiResponseCode.SERVER_ERROR))
                .onErrorMap(NullPointerException.class, e -> new RankingException(ApiResponseCode.SERVER_ERROR));
    }

    public Mono<Boolean> updatePriceDelta(Long stockId, String stockCode, BigDecimal buyPrice) {
        ReactiveZSetOperations<String, String> zSetOperations = reactiveRedisTemplate.opsForZSet();

        return stockRepository.findByIdAndCode(stockId, stockCode)
                .switchIfEmpty(Mono.error(new RankingException(ApiResponseCode.STOCK_NOT_FOUND))) // 주식이 존재하지 않을 경우 예외 발생
                .flatMap(stockEntity -> {
                    BigDecimal currentPrice = stockEntity.getPrice();
                    BigDecimal changePercent = calculatePriceDelta(buyPrice, currentPrice);

                    return zSetOperations.add(Const.STOCK_PRICE_DELTA_KEY, stockId + ":" + stockCode, changePercent.doubleValue());
                })
                .doOnError(e -> {
                    log.error("Failed to update price change for stock: " + stockCode, e);
                })
                .onErrorResume(e -> Mono.error(e));
    }

    public Flux<StockPriceDeltaRankApiDto> findAllStocksPriceDeltaRanking(SearchVo searchVo) {
        ReactiveZSetOperations<String, String> zSetOperations = reactiveRedisTemplate.opsForZSet();

        // get the range from sorted set in ascending order
        return zSetOperations.reverseRangeWithScores(Const.STOCK_PRICE_DELTA_KEY, Range.closed(searchVo.getStartIndex(), searchVo.getEndIndex()))
                .flatMap(tuple -> {
                    String[] split = tuple.getValue().split(":");
                    Long stockId = Long.valueOf(split[0]);
                    String stockCode = split[1];

                    return stockRepository.findByIdAndCode(stockId, stockCode)
                            .switchIfEmpty(Mono.error(new RankingException(ApiResponseCode.STOCK_NOT_FOUND)))
                            .map(stockEntity -> {
                                var stockPriceDeltaRankApiDto = stockConverter.toStockPriceDeltaRankApiDto(stockEntity);
                                stockPriceDeltaRankApiDto.setPriceDeltaPercentage(tuple.getScore());
                                return stockPriceDeltaRankApiDto;
                            });
                }).onErrorMap(NumberFormatException.class, e -> new RankingException(ApiResponseCode.SERVER_ERROR))
                .onErrorMap(NullPointerException.class, e -> new RankingException(ApiResponseCode.SERVER_ERROR));
    }


    public Mono<Double> findStockPriceDeltaByStockIdAndCode(Long stockId, String stockCode) {
        ReactiveZSetOperations<String, String> zSetOperations = reactiveRedisTemplate.opsForZSet();
        String key = stockId + ":" + stockCode;

        return zSetOperations.score(Const.STOCK_PRICE_DELTA_KEY, key);
    }

//    public Mono<StockPriceDeltaRankApiDto> findStockPriceDeltaByStockIdAndCode(Long stockId, String stockCode) {
//        ReactiveZSetOperations<String, String> zSetOperations = reactiveRedisTemplate.opsForZSet();
//        String key = stockId + ":" + stockCode;
//
//        return zSetOperations.score("stock:price:delta", key)
//                .switchIfEmpty(Mono.error(new RankingException(ApiResponseCode.STOCK_NOT_FOUND)))
//                .flatMap(score -> stockRepository.findByIdAndCode(stockId, stockCode)
//                        .switchIfEmpty(Mono.error(new RankingException(ApiResponseCode.STOCK_NOT_FOUND)))
//                        .map(stockEntity -> {
//                            var stockPriceDeltaRankApiDto = stockConverter.toStockPriceDeltaRankApiDto(stockEntity);
//                            stockPriceDeltaRankApiDto.setPriceDeltaPercentage(score);
//                            return stockPriceDeltaRankApiDto;
//                        })
//                )
//                .onErrorMap(NumberFormatException.class, e -> new RankingException(ApiResponseCode.SERVER_ERROR))
//                .onErrorMap(NullPointerException.class, e -> new RankingException(ApiResponseCode.SERVER_ERROR));
//    }

    private BigDecimal calculatePriceDelta(BigDecimal buyPrice, BigDecimal currentPrice) {

        if (buyPrice == null || currentPrice == null) {
            throw new RankingException(ApiResponseCode.CALCULATE_PRICE_ERROR);
        }
        if (buyPrice.equals(BigDecimal.ZERO) || currentPrice.equals(BigDecimal.ZERO)) {
            throw new RankingException(ApiResponseCode.CALCULATE_PRICE_ERROR);
        }

        return buyPrice.subtract(currentPrice)
                .divide(currentPrice, 2, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }



}
