package io.khw.rankingmodule.ranking.service;

import io.khw.common.constants.ApiResponseCode;
import io.khw.common.exeception.RankingException;
import io.khw.domain.stock.entity.StockEntity;
import io.khw.domain.stock.repository.StockRepository;
import org.junit.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveZSetOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

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

        when(reactiveRedisTemplate.opsForZSet()).thenReturn(zSetOperations);
        when(zSetOperations.incrementScore(anyString(), anyString(), anyDouble())).thenReturn(Mono.just(tradeVolume));

        // when
        StepVerifier.create(rankingService.increaseVolumeRank(stockId, stockCode, tradeVolume))
                // then
                .expectNext(tradeVolume)
                .verifyComplete();
    }

    @Test
    @DisplayName("종목 거래량 증가 실패")
    void increaseVolumeRankFail() {
        // given
        Long stockId = 42L;
        String stockCode = "377300";
        double tradeVolume = 14;

        when(reactiveRedisTemplate.opsForZSet()).thenReturn(zSetOperations);
        when(zSetOperations.incrementScore(anyString(), anyString(), anyDouble())).thenReturn(Mono.error(new RedisConnectionFailureException("Connection failure")));

        // when
        StepVerifier.create(rankingService.increaseVolumeRank(stockId, stockCode, tradeVolume))
                // then
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
        StockEntity findStockEntity = StockEntity.builder().id(42L).code("377300").name("카카오페이").price(new BigDecimal(68400)).build();


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
    @DisplayName("종목 인기 증가 실패(레디스 서버 오류)")
    void increasePopularityRankFail() {
        // given
        Long stockId = 42L;
        String stockCode = "377300";
        StockEntity findStockEntity = StockEntity.builder().id(42L).code("377300").name("카카오페이").price(new BigDecimal(68400)).build();


        when(stockRepository.findByIdAndCode(stockId, stockCode)).thenReturn(Mono.just(findStockEntity));
        when(reactiveRedisTemplate.opsForZSet()).thenReturn(zSetOperations);
        when(zSetOperations.incrementScore(anyString(), anyString(), anyDouble())).thenReturn(Mono.error(new RedisConnectionFailureException("Connection failure")));

        // when
        StepVerifier.create(rankingService.increasePopularityRank(stockId, stockCode))
                // then
                .expectError(RankingException.class)
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

        //findByIdAndCode 호출이 허용됨을 명시
        verify(stockRepository).findByIdAndCode(stockId, stockCode);
        //stockRepository의 findByIdAndCode 외에 상호작용이 없음을 검증
        verifyNoMoreInteractions(stockRepository);
        //zSetOperations 상호작용이 없음을 검증
        verifyNoInteractions(zSetOperations);
    }
}
