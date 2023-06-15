package io.khw.ranking.ranking.service;

import io.khw.common.constants.ApiResponseCode;
import io.khw.common.constants.Const;
import io.khw.common.enums.StockOrderEnum;
import io.khw.common.exeception.RankingException;
import io.khw.common.util.FormatUtil;
import io.khw.domain.common.vo.SearchVo;
import io.khw.domain.stock.dto.StockPriceDeltaRankApiDto;
import io.khw.domain.stock.entity.StockEntity;
import io.khw.domain.stock.repository.StockRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.ReactiveHashOperations;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveZSetOperations;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
public class RankingServiceTest {

    @Mock
    private ReactiveRedisTemplate<String, String> reactiveRedisTemplate;

    @Mock
    private ReactiveZSetOperations<String, String> zSetOperations;

    @Mock
    private ReactiveHashOperations<String, Object, Object> hashOperations;

    @Mock
    private StockRepository stockRepository;


    @InjectMocks
    private RankingService rankingService;


    @BeforeEach
    public void setUp() {
        when(reactiveRedisTemplate.opsForZSet()).thenReturn(zSetOperations);
        when(reactiveRedisTemplate.opsForHash()).thenReturn(hashOperations);
    }

    @Test
    @DisplayName("hash 오퍼레이션에 저장된 실시간 주식 정보(이름, 현재가, 상승,하락율) 조회 성공")
    public void findStockPriceShouldReturnPriceSuccess() {

        // Given
        Long stockId = 5930L;
        String stockCode = "삼성전자";
        String stockPriceJson = "{\"currentPrice\": \"61500\", \"percent\": \"0.00\"}";

        // When
        when(reactiveRedisTemplate.opsForHash()).thenReturn(hashOperations);
        when(hashOperations.get(Const.STOCK_PRICE_KEY, stockId + ":" + stockCode)).thenReturn(Mono.just(stockPriceJson));

        // Then
        StepVerifier.create(rankingService.findStockPrice(stockId, stockCode))
                .expectNextMatches(stockPrice ->
                        stockPrice.get("currentPrice").equals("61500") &&
                                stockPrice.get("percent").equals("0.00"))
                .verifyComplete();
    }

    @Test
    @DisplayName("hash 오퍼레이션에 저장된 실시간 주식 정보(이름, 현재가, 상승,하락율)와 DB에 저장된 주식정보 상세 조회 성공")
    public void findStockPriceDetailSuccess() {
        // Given
        Long stockId = 42L;
        String stockCode = "377300";
        String savedValueJson = "{\"name\":\"카카오페이\",\"currentPrice\":\"68400\",\"percent\":\"0.00\"}";
        StockEntity findStockEntity = StockEntity.builder().id(stockId).code(stockCode).name("카카오페이").price(new BigDecimal("68400")).build();
        double popularityScore = 1.0;

        // When
        when(stockRepository.findByIdAndCode(stockId, stockCode)).thenReturn(Mono.just(findStockEntity));
        when(reactiveRedisTemplate.opsForHash()).thenReturn(hashOperations);
        when(hashOperations.get(Const.STOCK_PRICE_KEY, stockId + ":" + stockCode)).thenReturn(Mono.just(savedValueJson));
        when(reactiveRedisTemplate.opsForZSet()).thenReturn(zSetOperations);
        when(zSetOperations.incrementScore(Const.STOCK_RANKING_POPULARITY_KEY, stockId + ":" + stockCode, 1)).thenReturn(Mono.just(popularityScore));

        // Then
        StepVerifier.create(rankingService.findStockPriceDetailAndUpdatePopularity(stockId, stockCode))
                .expectNextMatches(stockPriceDeltaRankApiDto ->
                        stockPriceDeltaRankApiDto.getCode().equals(stockCode) &&
                                stockPriceDeltaRankApiDto.getName().equals("카카오페이") &&
                                //stockPriceDeltaRankApiDto.getPrice().compareTo(new BigDecimal("68400")) == 0 &&
                                stockPriceDeltaRankApiDto.getPriceDeltaPercentage() == 0.00)
                .verifyComplete();
    }

    @Test
    @DisplayName("종목 가격, 거래량 랭킹, 가격 변동 업데이트 성공")
    public void updateStockPriceAndVolumeRankSuccess() {

        // Given
        Long stockId = 42L;
        String stockCode = "377300";
        BigDecimal newPrice = new BigDecimal("75240");
        int tradeVolume = 2000;

        StockEntity findStockEntity = StockEntity.builder().id(42L).code("377300").name("카카오페이").price(new BigDecimal("68400")).build();

        String savedValueJson = "{\"name\":\"카카오페이\",\"currentPrice\":\"68400\",\"percent\":\"10\"}";

        ReactiveZSetOperations<String, String> zSetOperations = mock(ReactiveZSetOperations.class);

        // When
        when(stockRepository.findByIdAndCode(stockId, stockCode)).thenReturn(Mono.just(findStockEntity));
        when(reactiveRedisTemplate.opsForHash()).thenReturn(hashOperations);
        when(reactiveRedisTemplate.opsForZSet()).thenReturn(zSetOperations);
        when(zSetOperations.incrementScore(anyString(), anyString(), anyDouble())).thenReturn(Mono.just(2000.0));
        when(hashOperations.get(anyString(), anyString())).thenReturn(Mono.just(savedValueJson));
        when(zSetOperations.add(anyString(), anyString(), anyDouble())).thenReturn(Mono.just(true));
        when(hashOperations.put(anyString(), anyString(), anyString())).thenReturn(Mono.just(true));

        // Then
        StepVerifier.create(rankingService.updateStockPriceAndVolumeRank(stockId, stockCode, newPrice, tradeVolume))
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    @DisplayName("종목 가격 업데이트 실패(주가정보 없음)")
    void updateStockPriceNotFoundStock() {
        // Given
        Long stockId = 42L;
        String stockCode = "377300";
        BigDecimal newPrice = new BigDecimal(65000);
        int tradeVolume = 20000;
        StockEntity findStockEntity = StockEntity.builder().id(42L).code("377300").name("카카오페이").price(new BigDecimal("68400")).build();

        // When
        when(stockRepository.findByIdAndCode(anyLong(), anyString()))
                .thenReturn(Mono.empty());


        // Then
        StepVerifier.create(rankingService.updateStockPriceAndVolumeRank(stockId, stockCode, newPrice, tradeVolume))
                .expectError(RankingException.class)
                .verify();
    }

    @Test
    @DisplayName("종목 가격 업데이트 (increaseVolumeRank) 실패")
    void updateStockPriceIncreaseVolumeRankFail() {
        // Given
        Long stockId = 42L;
        String stockCode = "377300";
        BigDecimal newPrice = new BigDecimal(65000);
        int tradeVolume = 20000;
        StockEntity findStockEntity = StockEntity.builder().id(42L).code("377300").name("카카오페이").price(new BigDecimal("68400")).build();

        // When
        when(stockRepository.findByIdAndCode(anyLong(), anyString()))
                .thenReturn(Mono.just(findStockEntity));

        when(reactiveRedisTemplate.opsForHash()).thenReturn(hashOperations);
        when(hashOperations.put(anyString(), anyString(), anyString())).thenReturn(Mono.just(true));
        when(zSetOperations.incrementScore(anyString(), anyString(), anyDouble()))
                .thenReturn(Mono.error(new RuntimeException()));

        // Then
        StepVerifier.create(rankingService.updateStockPriceAndVolumeRank(stockId, stockCode, newPrice, tradeVolume))
                .expectError(RankingException.class)
                .verify();
    }

    @Test
    @DisplayName("종목 가격 업데이트 (updatePriceDelta) 실패")
    void updateStockPriceUpdatePriceDeltaFail() {
        // Given
        Long stockId = 42L;
        String stockCode = "377300";
        BigDecimal newPrice = new BigDecimal(65000);
        int tradeVolume = 20000;
        StockEntity findStockEntity = StockEntity.builder().id(42L).code("377300").name("카카오페이").price(new BigDecimal("68400")).build();

        // When
        when(stockRepository.findByIdAndCode(anyLong(), anyString()))
                .thenReturn(Mono.just(findStockEntity));

        when(reactiveRedisTemplate.opsForHash()).thenReturn(hashOperations);
        when(hashOperations.put(anyString(), anyString(), anyString())).thenReturn(Mono.just(true));
        when(zSetOperations.incrementScore(anyString(), anyString(), anyDouble()))
                .thenReturn(Mono.just(1.0));

        when(hashOperations.get(anyString(), anyString()))
                .thenReturn(Mono.just("65000"));
        when(zSetOperations.add(anyString(), anyString(), anyDouble()))
                .thenReturn(Mono.error(new RuntimeException()));

        // Then
        StepVerifier.create(rankingService.updateStockPriceAndVolumeRank(stockId, stockCode, newPrice, tradeVolume))
                .expectError(RankingException.class)
                .verify();
    }

    @Test
    @DisplayName("종목 거래량 증가 성공")
    void increaseVolumeRankSuccess() {
        // Given
        Long stockId = 42L;
        String stockCode = "377300";
        int tradeVolume = 14;
        double incTradeVolume = 14;

        // When
        when(reactiveRedisTemplate.opsForZSet()).thenReturn(zSetOperations);
        when(zSetOperations.incrementScore(anyString(), anyString(), anyDouble())).thenReturn(Mono.just(incTradeVolume));

        // Then
        StepVerifier.create(rankingService.increaseVolumeRank(stockId, stockCode, tradeVolume))
                .expectNext(incTradeVolume)
                .verifyComplete();
    }

    @Test
    @DisplayName("종목 거래량 증가 실패(RedisConnectionFailureException 예외)")
    void increaseVolumeRankFail() {
        // Given
        Long stockId = 42L;
        String stockCode = "377300";
        int tradeVolume = 14;

        // When
        when(reactiveRedisTemplate.opsForZSet()).thenReturn(zSetOperations);
        when(zSetOperations.incrementScore(anyString(), anyString(), anyDouble())).thenReturn(Mono.error(new RedisConnectionFailureException("Connection failure")));

        // Then
        StepVerifier.create(rankingService.increaseVolumeRank(stockId, stockCode, tradeVolume))
                .expectError(RankingException.class)
                .verify();
    }

    @Test
    @DisplayName("종목 거래량 순위 조회 성공")
    public void findAllStocksInAscendingOrderSuccess() {
        // Given
        Long stockId = 42L;
        String stockCode = "377300";
        double priceDelta = 6.00;
        String combinedIdAndCode = stockId + ":" + stockCode;
        StockEntity findStockEntity = StockEntity.builder().id(42L).code("377300").name("카카오페이").price(new BigDecimal("68400")).build();
        StockPriceDeltaRankApiDto stockPriceDeltaRankApiDto = new StockPriceDeltaRankApiDto(findStockEntity.getId(), findStockEntity.getCode(), findStockEntity.getName(), FormatUtil.formatPriceToKoreanWon(findStockEntity.getPrice()), priceDelta);

        String stockPriceJson = "{\"name\": \"카카오페이\", \"currentPrice\": \"68400\", \"percent\": \"6.00\"}";

        // When
        when(reactiveRedisTemplate.opsForZSet()).thenReturn(zSetOperations);
        when(reactiveRedisTemplate.opsForHash()).thenReturn(hashOperations);

        when(zSetOperations.reverseRangeWithScores(anyString(), any())).thenReturn(Flux.just(ZSetOperations.TypedTuple.of(combinedIdAndCode, 1.0)));

        when(hashOperations.get(Const.STOCK_PRICE_KEY, stockId + ":" + stockCode)).thenReturn(Mono.just(stockPriceJson));

        when(rankingService.findStockPriceDeltaByStockIdAndCode(stockId, stockCode)).thenReturn(Mono.just(priceDelta));

        // Then
        StepVerifier.create(rankingService.findAllStocksVolumeRanking(new SearchVo(1,10)))
                .expectNextMatches(result -> result.getId().equals(stockPriceDeltaRankApiDto.getId())
                        && result.getCode().equals(stockPriceDeltaRankApiDto.getCode())
                        && result.getName().equals(stockPriceDeltaRankApiDto.getName())
                        && result.getPrice().equals(stockPriceDeltaRankApiDto.getPrice())
                        && result.getPriceDeltaPercentage() == stockPriceDeltaRankApiDto.getPriceDeltaPercentage())
                .verifyComplete();
    }

    @Test
    @DisplayName("종목 거래량 순위 조회 실패")
    public void findAllStocksInAscendingOrder_Failure() {
        // Given
        ReactiveZSetOperations<String, String> zSetOperations = mock(ReactiveZSetOperations.class);

        // When
        when(reactiveRedisTemplate.opsForZSet()).thenReturn(zSetOperations);
        when(zSetOperations.reverseRangeWithScores(anyString(), any())).thenReturn(Flux.empty());

        // Then
        StepVerifier.create(rankingService.findAllStocksVolumeRanking(new SearchVo(1,10)))
                .expectComplete();  // 결과가 없어도 에러가 아닌 complete를 반환
    }

    @Test
    @DisplayName("종목 거래량 순위 조회 실패(예외)")
    public void findAllStocksInAscendingOrder_Exception() {
        // Given
        Long stockId = 42L;
        String stockCode = "377300";
        String combinedIdAndCode = stockId + ":" + stockCode;
        ReactiveZSetOperations<String, String> zSetOperations = mock(ReactiveZSetOperations.class);

        // When
        when(reactiveRedisTemplate.opsForZSet()).thenReturn(zSetOperations);
        when(zSetOperations.reverseRangeWithScores(anyString(), any())).thenReturn(Flux.just(ZSetOperations.TypedTuple.of(combinedIdAndCode, 1.0)));
        when(stockRepository.findByIdAndCode(Long.valueOf(stockId), stockCode)).thenReturn(Mono.empty());

        // Then
        StepVerifier.create(rankingService.findAllStocksVolumeRanking(new SearchVo(1,10)))
                .expectError(RankingException.class)
                .verify();
    }

    @Test
    @DisplayName("종목 인기 증가 성공")
    void increasePopularityRankSuccess() {
        // Given
        Long stockId = 42L;
        String stockCode = "377300";
        double totalPopularity = 14;

        // When
        when(reactiveRedisTemplate.opsForZSet()).thenReturn(zSetOperations);
        when(zSetOperations.incrementScore(anyString(), anyString(), anyDouble())).thenReturn(Mono.just(totalPopularity));

        // Then
        StepVerifier.create(rankingService.increasePopularityRank(stockId, stockCode))
                .expectNext(totalPopularity)
                .verifyComplete();
    }

    @Test
    @DisplayName("종목 인기 증가 실패(RedisConnectionFailureException 예외)")
    void increasePopularityRankFail() {
        // Given
        Long stockId = 42L;
        String stockCode = "377300";

        // When
        when(reactiveRedisTemplate.opsForZSet()).thenReturn(zSetOperations);
        when(zSetOperations.incrementScore(anyString(), anyString(), anyDouble())).thenReturn(Mono.error(new RedisConnectionFailureException("Connection failure")));

        // Then
        StepVerifier.create(rankingService.increasePopularityRank(stockId, stockCode))
                .expectError(RankingException.class)
                .verify();
    }

    @Test
    @DisplayName("종목 인기 순위 조회 성공 findAllStocksPopularityRanking")
    void findAllStocksPopularityOrder_Success() {
        // Given
        SearchVo searchVo = new SearchVo(1,10);
        String stockCode = "377300";
        Long stockId = 42L;
        String stockName = "카카오페이";
        BigDecimal currentPrice = new BigDecimal("68400");
        Double percent = 6.00;
        String savedValueJson = "{\"name\":\"카카오페이\",\"currentPrice\":\"68400\",\"percent\":\"10\"}";

        StockPriceDeltaRankApiDto expectedDto = new StockPriceDeltaRankApiDto(stockId, stockCode, stockName, currentPrice, percent);

        Map<String, String> map = new HashMap<>();
        map.put("name", stockName);
        map.put("currentPrice", currentPrice.toString());
        map.put("percent", percent.toString());

        // When
        when(reactiveRedisTemplate.opsForZSet()).thenReturn(zSetOperations);
        when(zSetOperations.reverseRangeWithScores(anyString(), any(Range.class)))
                .thenReturn(Flux.just(ZSetOperations.TypedTuple.of(stockId + ":" + stockCode, percent)));

        when(reactiveRedisTemplate.opsForHash()).thenReturn(hashOperations);
        when(hashOperations.get(anyString(), anyString())).thenReturn(Mono.just(savedValueJson));

        Flux<StockPriceDeltaRankApiDto> actualFlux = rankingService.findAllStocksPopularityRanking(searchVo);

        // Then
        StepVerifier.create(actualFlux)
                .expectNextMatches(actualDto -> actualDto.getCode().equals(expectedDto.getCode())
                        && actualDto.getId().equals(expectedDto.getId())
                        && actualDto.getName().equals(expectedDto.getName())
                        && actualDto.getPrice().compareTo(expectedDto.getPrice()) == 0)
                .verifyComplete();
    }

    @Test
    @DisplayName("종목 인기 순위 조회 성공(reverseRangeWithScores 값 반환 없음)")
    void findAllStocksPopularityOrder_Failure() {
        // Given
        when(zSetOperations.reverseRangeWithScores(anyString(), any(Range.class))).thenReturn(Flux.empty());

        // When
        Flux<StockPriceDeltaRankApiDto> actual = rankingService.findAllStocksPopularityRanking(new SearchVo(1,10));

        // Then
        StepVerifier.create(actual)
                .verifyComplete();
    }

    @Test
    @DisplayName("종목 인기 순위 조회 실패(예외)")
    void findAllStocksPopularityOrder_Exception() {
        // Given
        when(zSetOperations.reverseRangeWithScores(anyString(), any(Range.class)))
                .thenReturn(Flux.error(new RuntimeException("Unexpected error")));

        // When
        Flux<StockPriceDeltaRankApiDto> actual = rankingService.findAllStocksPopularityRanking(new SearchVo(1,10));

        // Then
        StepVerifier.create(actual)
                .verifyError(RuntimeException.class);
    }


    @Test
    @DisplayName("주가 상승,하락폭 업데이트 성공 케이스(주가 상승)")
    public void updatePriceDeltaIncSuccess() {
        // Given
        Long stockId = 5930L;
        String stockCode = "삼성전자";
        BigDecimal buyPrice = new BigDecimal("120");
        BigDecimal currentPrice = new BigDecimal(100);
        String currentPriceString = "100";

        String stockPriceJson = "{\"currentPrice\": \"" + currentPriceString + "\", \"percent\": \"0.00\"}";

        // When
        when(reactiveRedisTemplate.opsForHash()).thenReturn(hashOperations);

        when(hashOperations.get(Const.STOCK_PRICE_KEY, stockId + ":" + stockCode)).thenReturn(Mono.just(stockPriceJson));

        ReactiveZSetOperations<String, String> mockZSetOperations = Mockito.mock(ReactiveZSetOperations.class);
        when(reactiveRedisTemplate.opsForZSet()).thenReturn(mockZSetOperations);

        when(mockZSetOperations.add(anyString(), anyString(), anyDouble())).thenReturn(Mono.just(true));

        // Then
        StepVerifier.create(rankingService.updatePriceDelta(stockId, stockCode, buyPrice, currentPrice))
                .expectNextMatches(changePercent -> changePercent.compareTo(new BigDecimal("20.00")) == 0)
                .verifyComplete();
    }

    @Test
    @DisplayName("주가 상승,하락폭 업데이트 성공 케이스(주가 하락)")
    public void updatePriceDeltaDecSuccess() {
        // Given
        Long stockId = 5930L;
        String stockCode = "삼성전자";
        BigDecimal buyPrice = new BigDecimal(100);
        BigDecimal currentPrice = new BigDecimal(120);
        String currentPriceString = "120";

        String stockPriceJson = "{\"currentPrice\": \"" + currentPriceString + "\", \"percent\": \"0.00\"}";

        // When
        when(reactiveRedisTemplate.opsForHash()).thenReturn(hashOperations);

        when(hashOperations.get(Const.STOCK_PRICE_KEY, stockId + ":" + stockCode)).thenReturn(Mono.just(stockPriceJson));

        ReactiveZSetOperations<String, String> mockZSetOperations = Mockito.mock(ReactiveZSetOperations.class);
        when(reactiveRedisTemplate.opsForZSet()).thenReturn(mockZSetOperations);

        when(mockZSetOperations.add(anyString(), anyString(), anyDouble())).thenReturn(Mono.just(true));

        // Then
        StepVerifier.create(rankingService.updatePriceDelta(stockId, stockCode, buyPrice, currentPrice))
                .expectNextMatches(changePercent -> changePercent.compareTo(new BigDecimal("-16.67")) == 0)
                .verifyComplete();
    }


    @Test
    @DisplayName("주가 상승,하락폭 실패 케이스 (구매 가격이 null and BigDecimal.ZERO)")
    void updatePriceDelta_fail_buyPriceLowerThanCurrent() {
        // Given
        Long stockId = 1L;
        String stockCode = "1234";
        BigDecimal buyPrice = null;
        BigDecimal currentPrice = new BigDecimal(65000);

        // When
        when(hashOperations.get(anyString(), anyString()))
                .thenReturn(Mono.just("65000"));

        // Then
        StepVerifier.create(rankingService.updatePriceDelta(stockId, stockCode, buyPrice, currentPrice))
                .expectError(RankingException.class)
                .verify();

    }

    @Test
    @DisplayName("주가 상승,하락폭 예외 RankingException")
    public void testUpdatePriceDeltaUpdateFailed() {
        // Given
        Long stockId = 42L;
        String stockCode = "377300";
        BigDecimal buyPrice = new BigDecimal(68000);
        BigDecimal currentPrice = new BigDecimal(65000);

        // When
        when(hashOperations.get(anyString(), anyString()))
                .thenReturn(Mono.just("65000"));
        when(zSetOperations.add(anyString(), anyString(), anyDouble())).thenReturn(Mono.error(new RuntimeException("Redis error")));

        // Then
        StepVerifier.create(rankingService.updatePriceDelta(stockId, stockCode, buyPrice, currentPrice))
                .expectError(RankingException.class)
                .verify();
    }


    @Test
    @DisplayName("주식 가격 변동 순위 조회 성공")
    void findAllStocksPriceDeltaOrderSuccess() {
        // Given
        SearchVo searchVo = new SearchVo(1,10);
        String stockCode = "377300";
        Long stockId = 42L;
        String stockName = "카카오페이";
        BigDecimal currentPrice = new BigDecimal("68400");
        Double percent = 6.00;
        String savedValueJson = "{\"name\":\"카카오페이\",\"currentPrice\":\"68400\",\"percent\":\"10\"}";

        StockPriceDeltaRankApiDto expectedDto = new StockPriceDeltaRankApiDto(stockId, stockCode, stockName, currentPrice, percent);

        Map<String, String> map = new HashMap<>();
        map.put("name", stockName);
        map.put("currentPrice", currentPrice.toString());
        map.put("percent", percent.toString());

        // When
        when(reactiveRedisTemplate.opsForZSet()).thenReturn(zSetOperations);
        when(zSetOperations.reverseRangeWithScores(anyString(), any(Range.class)))
                .thenReturn(Flux.just(ZSetOperations.TypedTuple.of(stockId + ":" + stockCode, percent)));

        when(reactiveRedisTemplate.opsForHash()).thenReturn(hashOperations);
        when(hashOperations.get(anyString(), anyString())).thenReturn(Mono.just(savedValueJson));

        Flux<StockPriceDeltaRankApiDto> actualFlux = rankingService.findAllStocksPriceDeltaRanking(searchVo, StockOrderEnum.INC.name());

        // Then
        StepVerifier.create(actualFlux)
                .expectNextMatches(actualDto -> actualDto.getCode().equals(expectedDto.getCode())
                        && actualDto.getId().equals(expectedDto.getId())
                        && actualDto.getName().equals(expectedDto.getName())
                        && actualDto.getPrice().compareTo(expectedDto.getPrice()) == 0)
                .verifyComplete();
    }

    @Test
    @DisplayName("주식 가격 변동 순위 조회 실패(예외)")
    void findAllStocksPriceDeltaOrder_NotFound() {
        // Given
        Long stockId = 42L;
        String stockCode = "377300";

        when(zSetOperations.reverseRangeWithScores(anyString(), any(Range.class)))
                .thenReturn(Flux.error(new RuntimeException("Redis error")));

        when(reactiveRedisTemplate.opsForHash()).thenReturn(hashOperations);

        // When
        Flux<StockPriceDeltaRankApiDto> actual = rankingService.findAllStocksPriceDeltaRanking(new SearchVo(1,10), StockOrderEnum.INC.name());

        // Then
        StepVerifier.create(actual)
                .expectError(RuntimeException.class)
                .verify();
    }


    @Test
    @DisplayName("주가 변동폭 계산 테스트")
    public void testCalculatePriceDelta() {

        BigDecimal newPrice = new BigDecimal("150.00");
        BigDecimal currentPrice = new BigDecimal("100.00");
        BigDecimal expectedIncrease = new BigDecimal("50.00");
        assertEquals(expectedIncrease, calculatePriceDelta(newPrice, currentPrice));

        newPrice = new BigDecimal("50.00");
        currentPrice = new BigDecimal("100.00");
        BigDecimal expectedDecrease = new BigDecimal("-50.00");
        assertEquals(expectedDecrease, calculatePriceDelta(newPrice, currentPrice));

        newPrice = new BigDecimal("100.00");
        currentPrice = new BigDecimal("100.00");
        BigDecimal expectedNoChange = new BigDecimal("0.00");
        assertEquals(expectedNoChange, calculatePriceDelta(newPrice, currentPrice));
    }

    @DisplayName("주가 변동폭 계산 메소드")
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
