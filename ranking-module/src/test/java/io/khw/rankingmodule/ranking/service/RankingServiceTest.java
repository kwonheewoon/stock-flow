package io.khw.rankingmodule.ranking.service;

import io.khw.common.constants.ApiResponseCode;
import io.khw.common.exeception.RankingException;
import io.khw.domain.stock.converter.StockConverter;
import io.khw.domain.stock.dto.StockApiDto;
import io.khw.domain.stock.dto.StockPriceDeltaRankApiDto;
import io.khw.domain.stock.entity.StockEntity;
import io.khw.domain.stock.repository.StockRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveZSetOperations;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(SpringExtension.class)
public class RankingServiceTest {

    @Mock
    private ReactiveRedisTemplate<String, String> reactiveRedisTemplate;

    @Mock
    private ReactiveZSetOperations<String, String> zSetOperations;

    @Mock
    private StockRepository stockRepository;

    @Mock
    private StockConverter stockConverter;

    @InjectMocks
    private RankingService rankingService;

    @BeforeEach
    public void setUp() {
        when(reactiveRedisTemplate.opsForZSet()).thenReturn(zSetOperations);
    }

    @Test
    @DisplayName("종목 거래량 증가 성공")
    void increaseVolumeRankSuccess() {
        // given
        Long stockId = 42L;
        String stockCode = "377300";
        double tradeVolume = 14;
        StockEntity findStockEntity = StockEntity.builder().id(42L).code("377300").name("카카오페이").price(new BigDecimal("68400")).build();

        when(stockRepository.findByIdAndCode(stockId, stockCode)).thenReturn(Mono.just(findStockEntity));
        when(reactiveRedisTemplate.opsForZSet()).thenReturn(zSetOperations);
        when(zSetOperations.incrementScore(anyString(), anyString(), anyDouble())).thenReturn(Mono.just(tradeVolume));

        // when
        StepVerifier.create(rankingService.increaseVolumeRank(stockId, stockCode, tradeVolume))
                // then
                .expectNext(tradeVolume)
                .verifyComplete();
    }

    @Test
    @DisplayName("종목 거래량 증가 실패(RedisConnectionFailureException 예외)")
    void increaseVolumeRankFail() {
        // given
        Long stockId = 42L;
        String stockCode = "377300";
        double tradeVolume = 14;
        StockEntity findStockEntity = StockEntity.builder().id(42L).code("377300").name("카카오페이").price(new BigDecimal("68400")).build();


        when(stockRepository.findByIdAndCode(stockId, stockCode)).thenReturn(Mono.just(findStockEntity));
        when(reactiveRedisTemplate.opsForZSet()).thenReturn(zSetOperations);
        when(zSetOperations.incrementScore(anyString(), anyString(), anyDouble())).thenReturn(Mono.error(new RedisConnectionFailureException("Connection failure")));

        // when
        StepVerifier.create(rankingService.increaseVolumeRank(stockId, stockCode, tradeVolume))
                // then
                .expectError(RedisConnectionFailureException.class)
                .verify();
    }

    @Test
    @DisplayName("종목 거래량 증가 실패(존재하지 않는 종목)")
    void increaseVolumeRankFailSecond() {
        // given
        Long stockId = 42L;
        String stockCode = "377300";
        double tradeVolume = 14;

        when(stockRepository.findByIdAndCode(stockId, stockCode)).thenReturn(Mono.empty());

        // when
        StepVerifier.create(rankingService.increaseVolumeRank(stockId, stockCode, tradeVolume))
                // then
                .expectError(RankingException.class)
                .verify();

        //findByIdAndCode 호출이 허용됨을 명시
        verify(stockRepository).findByIdAndCode(stockId, stockCode);
        //stockRepository의 findByIdAndCode 외에 상호작용이 없음을 검증
        verifyNoMoreInteractions(stockRepository);
        //zSetOperations 상호작용이 없음을 검증
        verifyNoInteractions(zSetOperations);
    }

    @Test
    @DisplayName("종목 거래량 순위 조회 성공")
    public void findAllStocksInAscendingOrder_Success() {
        // setup
        Long stockId = 42L;
        String stockCode = "377300";
        Double priceDelta = 6.00;
        String combinedIdAndCode = stockId + ":" + stockCode;
        StockEntity findStockEntity = StockEntity.builder().id(42L).code("377300").name("카카오페이").price(new BigDecimal("68400")).build();
        StockPriceDeltaRankApiDto stockPriceDeltaRankApiDto = new StockPriceDeltaRankApiDto(findStockEntity.getId(), findStockEntity.getCode(), findStockEntity.getName(), findStockEntity.getPrice(), priceDelta, findStockEntity.getCreatedAt(), findStockEntity.getUpdatedAt());


        ReactiveZSetOperations<String, String> zSetOperations = mock(ReactiveZSetOperations.class);

        when(reactiveRedisTemplate.opsForZSet()).thenReturn(zSetOperations);
        when(zSetOperations.reverseRangeWithScores(anyString(), any())).thenReturn(Flux.just(ZSetOperations.TypedTuple.of(combinedIdAndCode, 1.0)));
        when(stockRepository.findByIdAndCode(Long.valueOf(stockId), stockCode)).thenReturn(Mono.just(findStockEntity)); // Stock 객체를 실제 반환 타입으로 교체해야 합니다.
        when(stockConverter.toStockPriceDeltaRankApiDto(any())).thenReturn(stockPriceDeltaRankApiDto);
        when(rankingService.findStockPriceDeltaByStockIdAndCode(stockId, stockCode)).thenReturn(Mono.just(priceDelta));

        // execute
        StepVerifier.create(rankingService.findAllStocksInAscendingOrder(0, 1))
                .expectNext(stockPriceDeltaRankApiDto)
                .verifyComplete();
    }

    @Test
    @DisplayName("종목 거래량 순위 조회 실패")
    public void findAllStocksInAscendingOrder_Failure() {
        // setup
        ReactiveZSetOperations<String, String> zSetOperations = mock(ReactiveZSetOperations.class);

        when(reactiveRedisTemplate.opsForZSet()).thenReturn(zSetOperations);
        when(zSetOperations.reverseRangeWithScores(anyString(), any())).thenReturn(Flux.empty());

        // execute
        StepVerifier.create(rankingService.findAllStocksInAscendingOrder(0, 1))
                .expectComplete();  // 결과가 없어도 에러가 아닌 complete를 반환
    }

    @Test
    @DisplayName("종목 거래량 순위 조회 실패(예외)")
    public void findAllStocksInAscendingOrder_Exception() {
        // setup
        Long stockId = 42L;
        String stockCode = "377300";
        String combinedIdAndCode = stockId + ":" + stockCode;
        ReactiveZSetOperations<String, String> zSetOperations = mock(ReactiveZSetOperations.class);

        when(reactiveRedisTemplate.opsForZSet()).thenReturn(zSetOperations);
        when(zSetOperations.reverseRangeWithScores(anyString(), any())).thenReturn(Flux.just(ZSetOperations.TypedTuple.of(combinedIdAndCode, 1.0)));
        when(stockRepository.findByIdAndCode(Long.valueOf(stockId), stockCode)).thenReturn(Mono.empty());

        // execute
        StepVerifier.create(rankingService.findAllStocksInAscendingOrder(0, 1))
                .expectError(RankingException.class)
                .verify();
    }

    @Test
    @DisplayName("종목 인기 증가 성공")
    void increasePopularityRankSuccess() {
        // given
        Long stockId = 42L;
        String stockCode = "377300";
        double totalPopularity = 14;
        StockEntity findStockEntity = StockEntity.builder().id(42L).code("377300").name("카카오페이").price(new BigDecimal("68400")).build();


        when(stockRepository.findByIdAndCode(stockId, stockCode)).thenReturn(Mono.just(findStockEntity));
        when(reactiveRedisTemplate.opsForZSet()).thenReturn(zSetOperations);
        when(zSetOperations.incrementScore(anyString(), anyString(), anyDouble())).thenReturn(Mono.just(totalPopularity));

        // when
        StepVerifier.create(rankingService.increasePopularityRank(stockId, stockCode))
                // then
                .expectNext(totalPopularity)
                .verifyComplete();
    }

    @Test
    @DisplayName("종목 인기 증가 실패(RedisConnectionFailureException 예외)")
    void increasePopularityRankFail() {
        // given
        Long stockId = 42L;
        String stockCode = "377300";
        StockEntity findStockEntity = StockEntity.builder().id(42L).code("377300").name("카카오페이").price(new BigDecimal("68400")).build();


        when(stockRepository.findByIdAndCode(stockId, stockCode)).thenReturn(Mono.just(findStockEntity));
        when(reactiveRedisTemplate.opsForZSet()).thenReturn(zSetOperations);
        when(zSetOperations.incrementScore(anyString(), anyString(), anyDouble())).thenReturn(Mono.error(new RedisConnectionFailureException("Connection failure")));

        // when
        StepVerifier.create(rankingService.increasePopularityRank(stockId, stockCode))
                // then
                .expectError(RedisConnectionFailureException.class)
                .verify();
    }

    @Test
    @DisplayName("종목 인기 증가 실패(존재하지 않는 종목)")
    void increasePopularityRankFailSecond() {
        // given
        Long stockId = 42L;
        String stockCode = "377300";

        when(stockRepository.findByIdAndCode(stockId, stockCode)).thenReturn(Mono.empty());

        // when
        StepVerifier.create(rankingService.increasePopularityRank(stockId, stockCode))
                // then
                .expectError(RankingException.class)
                .verify();

        // then
        //findByIdAndCode 호출이 허용됨을 명시
        verify(stockRepository).findByIdAndCode(stockId, stockCode);
        //stockRepository의 findByIdAndCode 외에 상호작용이 없음을 검증
        verifyNoMoreInteractions(stockRepository);
        //zSetOperations 상호작용이 없음을 검증
        verifyNoInteractions(zSetOperations);
    }

    @Test
    @DisplayName("종목 인기 순위 조회 성공")
    void findAllStocksPopularityOrder_Success() {
        // given
        Long stockId = 42L;
        String stockCode = "377300";
        Double priceDelta = 6.00;
        StockEntity findStockEntity = StockEntity.builder().id(42L).code("377300").name("카카오페이").price(new BigDecimal("68400")).build();
        StockPriceDeltaRankApiDto expectedDto = new StockPriceDeltaRankApiDto(findStockEntity.getId(), findStockEntity.getCode(), findStockEntity.getName(), findStockEntity.getPrice(), priceDelta, findStockEntity.getCreatedAt(), findStockEntity.getUpdatedAt());

        when(zSetOperations.reverseRangeWithScores(anyString(), any(Range.class)))
                .thenReturn(Flux.just(ZSetOperations.TypedTuple.of(findStockEntity.getId() + ":" + findStockEntity.getCode(), 100.0)));
        when(stockRepository.findByIdAndCode(stockId, stockCode)).thenReturn(Mono.just(findStockEntity));
        when(stockConverter.toStockPriceDeltaRankApiDto(any(StockEntity.class))).thenReturn(expectedDto);
        when(rankingService.findStockPriceDeltaByStockIdAndCode(stockId, stockCode)).thenReturn(Mono.just(priceDelta));

        // when
        Flux<StockPriceDeltaRankApiDto> actual = rankingService.findAllStocksPopularityOrder(0L, 10L);

        // then
        StepVerifier.create(actual)
                .expectNext(expectedDto)
                .verifyComplete();
    }

    @Test
    @DisplayName("종목 인기 순위 조회 성공(reverseRangeWithScores 값 반환 없음)")
    void findAllStocksPopularityOrder_Failure() {
        // given
        when(zSetOperations.reverseRangeWithScores(anyString(), any(Range.class))).thenReturn(Flux.empty());

        // when
        Flux<StockPriceDeltaRankApiDto> actual = rankingService.findAllStocksPopularityOrder(0L, 10L);

        // then
        StepVerifier.create(actual)
                .verifyComplete();
    }

    @Test
    @DisplayName("종목 인기 순위 조회 실패(예외)")
    void findAllStocksPopularityOrder_Exception() {
        // given
        when(zSetOperations.reverseRangeWithScores(anyString(), any(Range.class)))
                .thenReturn(Flux.error(new RuntimeException("Unexpected error")));

        // when
        Flux<StockPriceDeltaRankApiDto> actual = rankingService.findAllStocksPopularityOrder(0L, 10L);

        // then
        StepVerifier.create(actual)
                .verifyError(RuntimeException.class);
    }

    @Test
    @DisplayName("주가 상승,하락폭 업데이트 성공 케이스(주가 상승)")
    void updatePriceDelta_success_increasing() {
        // given: 필요한 파라미터 설정
        Long stockId = 1L;
        String stockCode = "1234";
        BigDecimal buyPrice = new BigDecimal("110");
        StockEntity findStockEntity = StockEntity.builder().id(42L).code("377300").name("카카오페이").price(new BigDecimal("100")).build();


        when(stockRepository.findByIdAndCode(stockId, stockCode))
                .thenReturn(Mono.just(findStockEntity));
        when(zSetOperations.add(anyString(), anyString(), anyDouble()))
                .thenReturn(Mono.just(true));

        // when: 테스트 대상 메서드 호출
        StepVerifier.create(rankingService.updatePriceDelta(stockId, stockCode, buyPrice))
                // then: 결과 확인
                .expectNext(true)
                .verifyComplete();

        verify(stockRepository).findByIdAndCode(stockId, stockCode);
        verify(zSetOperations).add("stock:price:delta", stockId + ":" + stockCode, 10.00);
    }

    @Test
    @DisplayName("주가 상승,하락폭 업데이트 성공 케이스(주가 하락)")
    void updatePriceDelta_success_decreasing() {
        // given: 필요한 파라미터 설정
        Long stockId = 1L;
        String stockCode = "1234";
        BigDecimal buyPrice = new BigDecimal("90");
        StockEntity findStockEntity = StockEntity.builder().id(42L).code("377300").name("카카오페이").price(new BigDecimal("100")).build();


        when(stockRepository.findByIdAndCode(stockId, stockCode))
                .thenReturn(Mono.just(findStockEntity));
        when(zSetOperations.add(anyString(), anyString(), anyDouble()))
                .thenReturn(Mono.just(true));

        // when: 테스트 대상 메서드 호출
        StepVerifier.create(rankingService.updatePriceDelta(stockId, stockCode, buyPrice))
                // then: 결과 확인
                .expectNext(true)
                .verifyComplete();

        verify(stockRepository).findByIdAndCode(stockId, stockCode);
        verify(zSetOperations).add("stock:price:delta", stockId + ":" + stockCode, -10.00);
    }

    @Test
    @DisplayName("주가 상승,하락폭 실패 케이스 (구매 가격이 null and BigDecimal.ZERO)")
    void updatePriceDelta_fail_buyPriceLowerThanCurrent() {
        // given
        Long stockId = 1L;
        String stockCode = "1234";
        BigDecimal buyPrice = null;
        StockEntity findStockEntity = StockEntity.builder().id(42L).code("377300").name("카카오페이").price(new BigDecimal("100")).build();


        when(stockRepository.findByIdAndCode(stockId, stockCode))
                .thenReturn(Mono.just(findStockEntity));

        // when
        StepVerifier.create(rankingService.updatePriceDelta(stockId, stockCode, buyPrice))
                // then
                .expectError(RankingException.class)
                .verify();

        verify(stockRepository).findByIdAndCode(stockId, stockCode);
    }

    @Test
    @DisplayName("주가 상승,하락폭 예외 케이스 (주식 없음)")
    void updatePriceDelta_fail_stockNotFound() {
        // given
        Long stockId = 1L;
        String stockCode = "1234";
        BigDecimal buyPrice = new BigDecimal("110");

        when(stockRepository.findByIdAndCode(stockId, stockCode))
                .thenReturn(Mono.empty());

        // when
        StepVerifier.create(rankingService.updatePriceDelta(stockId, stockCode, buyPrice))
                // then
                .expectError(RankingException.class)
                .verify();

        verify(stockRepository).findByIdAndCode(stockId, stockCode);
    }

    @Test
    @DisplayName("주식 가격 변동 순위 조회 성공")
    void findAllStocksPriceDeltaOrder_Success() {
        // given
        Long stockId = 42L;
        String stockCode = "377300";
        StockEntity findStockEntity = StockEntity.builder().id(42L).code("377300").name("카카오페이").price(new BigDecimal("68400")).build();
        StockPriceDeltaRankApiDto expectedDto = new StockPriceDeltaRankApiDto(findStockEntity.getId(), findStockEntity.getCode(), findStockEntity.getName(), findStockEntity.getPrice(), 6.00, findStockEntity.getCreatedAt(), findStockEntity.getUpdatedAt());

        when(zSetOperations.reverseRangeWithScores(anyString(), any(Range.class)))
                .thenReturn(Flux.just(ZSetOperations.TypedTuple.of(findStockEntity.getId() + ":" + findStockEntity.getCode(), 100.0)));
        when(stockRepository.findByIdAndCode(stockId, stockCode)).thenReturn(Mono.just(findStockEntity));
        when(stockConverter.toStockPriceDeltaRankApiDto(any(StockEntity.class))).thenReturn(expectedDto);

        // when
        Flux<StockPriceDeltaRankApiDto> actual = rankingService.findAllStocksPriceDeltaOrder(0L, 10L);

        // then
        StepVerifier.create(actual)
                .expectNext(expectedDto)
                .verifyComplete();
    }

    @Test
    @DisplayName("주식 가격 변동 순위 조회 실패 - 주식 정보 없음")
    void findAllStocksPriceDeltaOrder_NotFound() {
        // given
        Long stockId = 42L;
        String stockCode = "377300";

        when(zSetOperations.reverseRangeWithScores(anyString(), any(Range.class)))
                .thenReturn(Flux.just(ZSetOperations.TypedTuple.of(stockId + ":" + stockCode, 100.0)));
        when(stockRepository.findByIdAndCode(stockId, stockCode)).thenReturn(Mono.empty());

        // when
        Flux<StockPriceDeltaRankApiDto> actual = rankingService.findAllStocksPriceDeltaOrder(0L, 10L);

        // then
        StepVerifier.create(actual)
                .expectError(RankingException.class)
                .verify();
    }


    @Test
    @DisplayName("주가 변동폭 계산 테스트")
    public void testCalculatePriceDelta() {
        // Test scenario where newPrice > currentPrice (price increase)
        BigDecimal newPrice = new BigDecimal("150.00");
        BigDecimal currentPrice = new BigDecimal("100.00");
        BigDecimal expectedIncrease = new BigDecimal("50.00");
        assertEquals(expectedIncrease, calculatePriceDelta(newPrice, currentPrice));

        // Test scenario where newPrice < currentPrice (price decrease)
        newPrice = new BigDecimal("50.00");
        currentPrice = new BigDecimal("100.00");
        BigDecimal expectedDecrease = new BigDecimal("-50.00");
        assertEquals(expectedDecrease, calculatePriceDelta(newPrice, currentPrice));

        // Test scenario where newPrice = currentPrice (no price change)
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

        return buyPrice.subtract(currentPrice)
                .divide(currentPrice, 2, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }
}
