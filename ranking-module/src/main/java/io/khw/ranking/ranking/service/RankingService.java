package io.khw.ranking.ranking.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.khw.common.constants.ApiResponseCode;
import io.khw.common.constants.Const;
import io.khw.common.enums.StockOrderEnum;
import io.khw.common.exeception.RankingException;
import io.khw.domain.common.vo.SearchVo;
import io.khw.domain.stock.converter.StockConverter;
import io.khw.domain.stock.dto.StockPriceDeltaRankApiDto;
import io.khw.domain.stock.repository.StockRepository;
import io.swagger.v3.core.util.Json;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.core.ReactiveHashOperations;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveZSetOperations;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class RankingService {

    private final StockRepository stockRepository;

    private final ReactiveRedisTemplate<String, String> reactiveRedisTemplate;


    /**
     * 실시간 주식 정보 단일 단일 조회 메소드
     *
     * 1.{"name":"카카오페이","percent":"0.00","currentPrice":"68400.00"} 형식으로 저장된 주식 데이터 redis에서 조회
     *
     * @param stockId 주식 아이디
     * @param stockCode 주식 코드
     * @return Mono<Map<String,String>>
     * @throws RuntimeException
     */
    public Mono<Map<String,String>> findStockPrice(Long stockId, String stockCode) {
        ReactiveHashOperations<String, String, String> hashOperations = reactiveRedisTemplate.opsForHash();
        return hashOperations.get(Const.STOCK_PRICE_KEY, stockId + ":" + stockCode)
                .map(json -> {
                    try {
                        ObjectMapper objectMapper = new ObjectMapper();
                        return objectMapper.readValue(json, new TypeReference<>() {
                        });
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException("JSON 파싱 오류", e);
                    }
                });
    }

    /**
     * 실시간 주식 정보 단일 단일 조회 및 인기순위 증가 메소드
     *
     * 1.DB에서 주식 상세정보 단일조회
     * 2.조회된 주식 정보 기준 인기순위 증가
     * 3.{"name":"카카오페이","percent":"0.00","currentPrice":"68400.00"} 형식으로 저장된 주식 데이터 redis에서 조회
     * 4.StockPriceDeltaRankApiDto 객체로 변환후 반환
     *
     * @param stockId 주식 아이디
     * @param stockCode 주식 코드
     * @return Mono<StockPriceDeltaRankApiDto>
     * @throws RuntimeException
     */
    public Mono<StockPriceDeltaRankApiDto> findStockPriceDetailAndUpdatePopularity(Long stockId, String stockCode) {
        return stockRepository.findByIdAndCode(stockId, stockCode)
                .switchIfEmpty(Mono.error(new RankingException(ApiResponseCode.STOCK_NOT_FOUND)))
                .flatMap(stockEntity ->
                        // 주식 인기도 증가
                        increasePopularityRank(stockEntity.getId(), stockEntity.getCode()).then(
                            findStockPrice(stockEntity.getId(), stockEntity.getCode()) // 실시간 주식정보 조회
                            .map(priceDelta -> new StockPriceDeltaRankApiDto(stockEntity.getId(), stockEntity.getCode(), priceDelta.get("name"), new BigDecimal(priceDelta.get("currentPrice")), Double.parseDouble(priceDelta.get("percent"))))
                        )
                );
    }

    /**
     * 실시간 주식 거래 메소드
     *
     * 1.DB에서 주식 상세정보 단일조회
     * 2.조회된 주식 정보 기준 거래량 증가
     * 3.현재 주가 기준으로 구매 가격대비 상승,하락폭 계산후 저장
     * 3.{"name":"카카오페이","percent":"0.00","currentPrice":"68400.00"} 형식으로 저장된 주식 데이터 업데이트(상승,하락폭 현재 주가)
     *
     * @param stockId 주식 아이디
     * @param stockCode 주식 코드
     * @param buyPrice 구매 가격
     * @param tradeVolume 구매 수량
     *
     * @return Mono<Boolean>
     */
    public Mono<Boolean> updateStockPriceAndVolumeRank(Long stockId, String stockCode, BigDecimal buyPrice, int tradeVolume) {
        return stockRepository.findByIdAndCode(stockId, stockCode) // 주식 정보 조회
                .switchIfEmpty(Mono.error(new RankingException(ApiResponseCode.STOCK_NOT_FOUND)))
                // 주식 거래량 증가
                .flatMap(stockEntity -> increaseVolumeRank(stockId, stockCode, tradeVolume)
                        // 주식 상승,하락폭 업데이트
                        .flatMap(success -> updatePriceDelta(stockId, stockCode, buyPrice, stockEntity.getPrice()))
                        .flatMap(changePercent -> {
                            Map<String, String> savedValueMap = Map.of("name", stockEntity.getName(), "currentPrice", buyPrice.toString(), "percent", changePercent.toString());
                            String savedValueJson;
                            try {
                                ObjectMapper objectMapper = new ObjectMapper();
                                savedValueJson = objectMapper.writeValueAsString(savedValueMap);
                            } catch (JsonProcessingException e) {
                                return Mono.error(new RuntimeException(e));
                            }

                            // 주식 정보(주식명칭, 현재 주식가격, 주가 상승,하락폭)
                            return reactiveRedisTemplate.opsForHash()
                                    .put(Const.STOCK_PRICE_KEY, stockId + ":" + stockCode, savedValueJson)
                                    .onErrorResume(e -> Mono.error(new RankingException(ApiResponseCode.UPDATE_PRICE_FAIL)))
                                    .switchIfEmpty(Mono.error(new RankingException(ApiResponseCode.UPDATE_PRICE_FAIL)));
                        }));
    }

    /**
     * 주식 거래량 업데이트 메소드
     *
     * 1.주식 거래량 증가
     *
     * @param stockId 주식 아이디
     * @param stockCode 주식 코드
     * @param tradeVolume 구매 수량
     *
     * @return Mono<StockPriceDeltaRankApiDto>
     * @throws RankingException
     */
    public Mono<Double> increaseVolumeRank(Long stockId, String stockCode, int tradeVolume) {
        ReactiveZSetOperations<String, String> zSetOperations = reactiveRedisTemplate.opsForZSet();

        return zSetOperations.incrementScore(Const.STOCK_RANKING_VOLUME_KEY, stockId + ":" + stockCode, tradeVolume)
                .doOnError(e -> {
                    log.error("종목 거래량 증가 실패 : {} , message : {}", stockCode, e);
                })
                .onErrorResume(e -> Mono.error(new RankingException(ApiResponseCode.INCREASE_VOLUME_RANK_FAIL)));
    }

    /**
     * 주식 거래량 순위 조회 메소드
     *
     * 1.주식 거래량 내림차순 조회
     * 2.조회된 데이터별 실시간 주식정보 상세조회
     *
     * @param searchVo SearchVo(page,size) 검색 객체
     *
     * @return Flux<StockPriceDeltaRankApiDto>
     * @throws RankingException
     */
    public Flux<StockPriceDeltaRankApiDto> findAllStocksVolumeRanking(SearchVo searchVo) {
        ReactiveZSetOperations<String, String> zSetOperations = reactiveRedisTemplate.opsForZSet();

        return zSetOperations.reverseRangeWithScores(Const.STOCK_RANKING_VOLUME_KEY, Range.closed(searchVo.getStartIndex(), searchVo.getEndIndex()))
                .flatMap(tuple -> {
                    String[] split = tuple.getValue().split(":");
                    Long stockId = Long.valueOf(split[0]);
                    String stockCode = split[1];

                    // 실시간 주식정보 조회, Dto 객체로 변환후 반환
                    return findStockPrice(stockId, stockCode)
                                    .map(priceDelta -> new StockPriceDeltaRankApiDto(stockId, stockCode, priceDelta.get("name"), new BigDecimal(priceDelta.get("currentPrice")), Double.parseDouble(priceDelta.get("percent"))));
                })
                .onErrorMap(NumberFormatException.class, e -> new RankingException(ApiResponseCode.SERVER_ERROR))
                .onErrorMap(NullPointerException.class, e -> new RankingException(ApiResponseCode.SERVER_ERROR));
    }

    /**
     * 주식 인기순위 증가 메소드
     *
     * 1.주식 인기순위 증가
     *
     * @param stockId 주식 아이디
     * @param stockCode 주식 코드
     *
     * @return Mono<Double>
     * @throws RankingException
     */
    public Mono<Double> increasePopularityRank(Long stockId, String stockCode) {
        ReactiveZSetOperations<String, String> zSetOperations = reactiveRedisTemplate.opsForZSet();

        return zSetOperations.incrementScore(Const.STOCK_RANKING_POPULARITY_KEY, stockId + ":" + stockCode, 1)
                .doOnError(e -> {
                    log.error("종목 인기 증가 실패 : {} , message : {}", stockCode, e);
                })
                .onErrorResume(e -> Mono.error(new RankingException(ApiResponseCode.INCREASE_POPULARITY_RANK_FAIL)));
    }

    /**
     * 주식 인기 순위 조회 메소드
     *
     * 1.주식 인기순위 내림차순 조회
     * 2.조회된 데이터별 실시간 주식정보 상세조회
     *
     * @param searchVo SearchVo(page,size) 검색 객체
     *
     * @return Flux<StockPriceDeltaRankApiDto>
     * @throws RankingException
     */
    public Flux<StockPriceDeltaRankApiDto> findAllStocksPopularityRanking(SearchVo searchVo) {
        ReactiveZSetOperations<String, String> zSetOperations = reactiveRedisTemplate.opsForZSet();

        return zSetOperations.reverseRangeWithScores(Const.STOCK_RANKING_POPULARITY_KEY, Range.closed(searchVo.getStartIndex(), searchVo.getEndIndex()))
                .flatMap(tuple -> {
                    String[] split = tuple.getValue().split(":");
                    Long stockId = Long.valueOf(split[0]);
                    String stockCode = split[1];

                    // 실시간 주식정보 조회, Dto 객체로 변환후 반환
                    return findStockPrice(stockId, stockCode)
                            .map(priceDelta -> new StockPriceDeltaRankApiDto(stockId, stockCode, priceDelta.get("name"), new BigDecimal(priceDelta.get("currentPrice")), Double.parseDouble(priceDelta.get("percent"))));
                }).onErrorMap(NumberFormatException.class, e -> new RankingException(ApiResponseCode.SERVER_ERROR))
                .onErrorMap(NullPointerException.class, e -> new RankingException(ApiResponseCode.SERVER_ERROR));
    }

    /**
     * 실시간 주가 상승,하락폭 정보 업데이트 메소드
     *
     * 1.실시간 주식 정보 조회
     * 2.병렬처리로 상승 zset, 하락 zset에 주가 상승,하락폭 업데이트
     *
     * @param stockId 주식 아이디
     * @param stockCode 주식 코드
     * @param buyPrice 구매 금액
     * @param currentPrice 현재 주가
     *
     * @return Mono<BigDecimal>
     * @throws RankingException
     */
    public Mono<BigDecimal> updatePriceDelta(Long stockId, String stockCode, BigDecimal buyPrice, BigDecimal currentPrice) {
            ReactiveZSetOperations<String, String> zSetOperations = reactiveRedisTemplate.opsForZSet();

            return findStockPrice(stockId, stockCode) // 실시간 주식정보 조회
                    .switchIfEmpty(Mono.error(new RankingException(ApiResponseCode.STOCK_PRICE_NOT_FOUND))) // 주식이 존재하지 않을 경우 예외 발생
                    .flatMap(stockInfo -> {
                        // 상승 혹은 하락폭 퍼센트 계산
                        BigDecimal changePercent = calculatePriceDelta(buyPrice, currentPrice);

                        // 상승, 하락 key 값에 퍼센트 병렬 업데이트 처리
                        // 데이터 조회시 내림차순, 오름차순으로 나뉘므로 동시에 업데이트해 데이터 일관성 맞춤
                        return Mono.zip(
                                        zSetOperations.add(Const.STOCK_INC_PRICE_DELTA_KEY, stockId + ":" + stockCode, changePercent.doubleValue())
                                                .doOnError(e -> log.error("종목 상승,하락가 업데이트 실패 : {} , message : {}", stockCode, e.getMessage())),
                                        zSetOperations.add(Const.STOCK_DEC_PRICE_DELTA_KEY, stockId + ":" + stockCode, changePercent.doubleValue())
                                                .doOnError(e -> log.error("종목 상승,하락가 업데이트 실패 : {} , message : {}", stockCode, e.getMessage()))
                                )
                                .thenReturn(changePercent);
                    })
                    .doOnError(e -> {
                        log.error("종목 상승,하락가 업데이트 실패 : {} , message : {}", stockCode, e.getMessage());
                    })
                    .onErrorResume(e -> Mono.error(new RankingException(ApiResponseCode.UPDATE_PRICE_DELTA_RANK_FAIL)));
    }

    /**
     * 주식 상승,하락폭 순위 조회 메소드
     *
     * 1.orderType별 상승,하락폭 순위 조회
     * 2.조회된 데이터별 실시간 주식정보 상세조회
     *
     * @param searchVo SearchVo(page,size) 검색 객체
     * @param orderType 상승,하락 조회 구분
     *
     * @return Flux<StockPriceDeltaRankApiDto>
     * @throws RankingException
     */
    public Flux<StockPriceDeltaRankApiDto> findAllStocksPriceDeltaRanking(SearchVo searchVo, String orderType) {
        ReactiveZSetOperations<String, String> zSetOperations = reactiveRedisTemplate.opsForZSet();

        Flux<ZSetOperations.TypedTuple<String>> zSetOper;

        // 상승, 하락 조회 구분
        if (orderType.equals(StockOrderEnum.INC.name())) {
            zSetOper = zSetOperations.reverseRangeWithScores(Const.STOCK_INC_PRICE_DELTA_KEY, Range.closed(searchVo.getStartIndex(), searchVo.getEndIndex()));
        } else {
            zSetOper = zSetOperations.rangeWithScores(Const.STOCK_DEC_PRICE_DELTA_KEY, Range.closed(searchVo.getStartIndex(), searchVo.getEndIndex()));
        }

        return zSetOper
                .flatMap(tuple -> {
                    String[] split = tuple.getValue().split(":");
                    Long stockId = Long.valueOf(split[0]);
                    String stockCode = split[1];

                    // 실시간 주식정보 조회, Dto 객체로 변환후 반환
                    return findStockPrice(stockId, stockCode)
                            .map(priceDelta -> new StockPriceDeltaRankApiDto(stockId, stockCode, priceDelta.get("name"), new BigDecimal(priceDelta.get("currentPrice")), Double.parseDouble(priceDelta.get("percent"))));
                }).onErrorMap(NumberFormatException.class, e -> new RankingException(ApiResponseCode.SERVER_ERROR))
                .onErrorMap(NullPointerException.class, e -> new RankingException(ApiResponseCode.SERVER_ERROR));
    }


    public Mono<Double> findStockPriceDeltaByStockIdAndCode(Long stockId, String stockCode) {
        ReactiveZSetOperations<String, String> zSetOperations = reactiveRedisTemplate.opsForZSet();
        String key = stockId + ":" + stockCode;

        return zSetOperations.score(Const.STOCK_PRICE_DELTA_KEY, key);
    }


    /**
     * 구매 가격, 현재 주가 상승, 하락폭 계산 메소드
     *
     * @param buyPrice 구매 가격
     * @param currentPrice 현재 주가
     *
     * @return BigDecimal
     * @throws RankingException
     */
    private BigDecimal calculatePriceDelta(BigDecimal buyPrice, BigDecimal currentPrice) {

        if (buyPrice == null || currentPrice == null) {
            throw new RankingException(ApiResponseCode.CALCULATE_PRICE_ERROR);
        }
        if (buyPrice.equals(BigDecimal.ZERO) || currentPrice.equals(BigDecimal.ZERO)) {
            throw new RankingException(ApiResponseCode.CALCULATE_PRICE_ERROR);
        }

        BigDecimal result = buyPrice.subtract(currentPrice)
                .divide(currentPrice, 10, RoundingMode.HALF_DOWN)
                .multiply(BigDecimal.valueOf(100));
        result = result.setScale(2, RoundingMode.HALF_DOWN);
        return result;
    }



}
