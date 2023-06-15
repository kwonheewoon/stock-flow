package io.khw.ranking.ranking.handler;

import io.khw.common.constants.ApiResponseCode;
import io.khw.common.exeception.RankingException;
import io.khw.common.util.FormatUtil;
import io.khw.domain.common.vo.SearchVo;
import io.khw.domain.stock.dto.*;
import io.khw.domain.stock.entity.StockEntity;
import io.khw.ranking.ranking.service.RankingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)

public class RankingHandlerTest {

    @Mock
    private RankingService rankingService;

    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        RankingHandler rankingHandler = new RankingHandler(rankingService);
        RouterFunction<ServerResponse> routerFunction = RouterFunctions.route()
                .PUT("/stocks/rankings/price-delta", rankingHandler::updatePriceDelta)
                .PUT("/stocks/rankings/volume/increase", rankingHandler::increaseVolumeRank)
                .PUT("/stocks/rankings/popularity/increase", rankingHandler::increasePopularityRank)
                .GET("/stocks/rankings/volume", rankingHandler::findAllStocksVolumeRanking)
                .GET("/stocks/rankings/popularity", rankingHandler::findAllStocksPopularityRanking)
                .GET("/stocks/rankings/price-delta", rankingHandler::findAllStocksPriceDeltaRanking)
                .POST("/stocks/rankings/{stockId}/{stockCode}", rankingHandler::updateStockPriceAndVolumeRank)
                .GET("/stocks/{stockId}/{stockCode}", rankingHandler::findStockPriceDetailAndUpdatePopularity)
                .build();

        webTestClient = WebTestClient.bindToRouterFunction(routerFunction).build();
    }

    @Test
    @DisplayName("주식 정보 상세조회(조회시 인기순위 증가)")
    void findStockPriceDetailAndUpdatePopularitySuccess() {
        // Given
        Long stockId = 42L;
        String stockCode = "377300";
        StockEntity stockEntity = StockEntity.builder()
                .id(42L)
                .code("377300")
                .name("카카오페이")
                .price(new BigDecimal("1000"))
                .build();

        StockPriceDeltaRankApiDto dto = new StockPriceDeltaRankApiDto(stockEntity.getId(), stockEntity.getCode(), stockEntity.getName(), FormatUtil.formatPriceToKoreanWon(stockEntity.getPrice()), 6.00);

        when(rankingService.findStockPriceDetailAndUpdatePopularity(anyLong(), anyString())).thenReturn(Mono.just(dto));

        // When
        webTestClient.get()
                .uri("/stocks/{stockId}/{stockCode}", stockId, stockCode)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.result.id").isEqualTo(dto.getId())
                .jsonPath("$.result.code").isEqualTo(dto.getCode())
                .jsonPath("$.result.name").isEqualTo(dto.getName())
                .jsonPath("$.result.price").isEqualTo(dto.getPrice())
                .jsonPath("$.result.priceDeltaPercentage").isEqualTo(dto.getPriceDeltaPercentage());

        // Then
        verify(rankingService).findStockPriceDetailAndUpdatePopularity(anyLong(), anyString());
        verifyNoMoreInteractions(rankingService);
    }

    @Test
    @DisplayName("주식 거래량 조회")
    void findAllStocksVolumeRankingSuccess() {
        // Given
        SearchVo searchVo = new SearchVo(1, 10);
        StockEntity stockEntity = StockEntity.builder()
                .id(42L)
                .code("377300")
                .name("카카오페이")
                .price(new BigDecimal("1000"))
                .build();

        StockPriceDeltaRankApiDto dto = new StockPriceDeltaRankApiDto(stockEntity.getId(), stockEntity.getCode(), stockEntity.getName(), FormatUtil.formatPriceToKoreanWon(stockEntity.getPrice()), 6.00);

        when(rankingService.findAllStocksVolumeRanking(any(SearchVo.class))).thenReturn(Flux.just(dto));

        // When
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/stocks/rankings/volume")
                        .queryParam("page", searchVo.getPage())
                        .queryParam("size", searchVo.getSize())
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.result.length()").isEqualTo(1)
                .jsonPath("$.result[0].id").isEqualTo(dto.getId())
                .jsonPath("$.result[0].code").isEqualTo(dto.getCode())
                .jsonPath("$.result[0].name").isEqualTo(dto.getName())
                .jsonPath("$.result[0].price").isEqualTo(dto.getPrice())
                .jsonPath("$.result[0].priceDeltaPercentage").isEqualTo(dto.getPriceDeltaPercentage());

        // Then
        verify(rankingService).findAllStocksVolumeRanking(any(SearchVo.class));
        verifyNoMoreInteractions(rankingService);
    }

    @Test
    @DisplayName("주식 거래량 순위 조회 실패(예외)")
    void findAllStocksVolumeRankingSucess() {
        // Given
        SearchVo searchVo = new SearchVo(1, 10);

        when(rankingService.findAllStocksVolumeRanking(any(SearchVo.class))).thenThrow(new RankingException(ApiResponseCode.STOCK_NOT_FOUND));

        // When
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/stocks/rankings/volume")
                        .queryParam("page", searchVo.getPage())
                        .queryParam("size", searchVo.getSize())
                        .build())
                .exchange()
                .expectStatus().is5xxServerError(); // Assuming that the RankingException maps to a 4xx HTTP status

        // Then
        verify(rankingService).findAllStocksVolumeRanking(any(SearchVo.class));
    }

    @Test
    @DisplayName("주식 거래(거래량, 주가, 상승,하락폭 업데이트) 성공")
    void updateStockPriceAndVolumeRankSuccess() {
        // Given
        Long stockId = 42L;
        String stockCode = "377300";
        BigDecimal newPrice = new BigDecimal(65000);
        int tradeVolume = 1000;
        StockPriceVolumeDto stockPriceVolumeDto = new StockPriceVolumeDto(stockId, stockCode, newPrice, tradeVolume);

        when(rankingService.updateStockPriceAndVolumeRank(stockId, stockCode, newPrice, tradeVolume)).thenReturn(Mono.just(true));

        // When
        webTestClient.post()
                .uri("/stocks/rankings/{stockId}/{stockCode}", stockId, stockCode)
                .body(Mono.just(stockPriceVolumeDto), StockPriceVolumeDto.class)
                .exchange()
                .expectStatus().isOk();

        // Then
        verify(rankingService).updateStockPriceAndVolumeRank(stockId, stockCode, newPrice, tradeVolume);
    }

    @Test
    @DisplayName("주식 거래량 증가 성공")
    void increaseVolumeRankSuccess() {
        // Given
        Long stockId = 42L;
        String stockCode = "377300";
        int tradeVolume = 1000;
        StockIncVolumeDto stockIncVolumeDto = new StockIncVolumeDto(stockId, stockCode, tradeVolume);

        when(rankingService.increaseVolumeRank(stockId, stockCode, tradeVolume)).thenReturn(Mono.just(500.0));

        // When
        webTestClient.put()
                .uri("/stocks/rankings/volume/increase")
                .body(Mono.just(stockIncVolumeDto), StockIncVolumeDto.class)
                .exchange()
                .expectStatus().isOk();

        // Then
        verify(rankingService).increaseVolumeRank(stockId, stockCode, tradeVolume);
    }


    @Test
    @DisplayName("주식 인기순 조회 성공")
    void findAllStocksPopularityRankingSuccess() {
        // Given
        SearchVo searchVo = new SearchVo(1, 10);
        StockEntity stockEntity = StockEntity.builder()
                .id(42L)
                .code("377300")
                .name("카카오페이")
                .price(new BigDecimal("1000"))
                .build();

        StockPriceDeltaRankApiDto dto = new StockPriceDeltaRankApiDto(stockEntity.getId(), stockEntity.getCode(), stockEntity.getName(), FormatUtil.formatPriceToKoreanWon(stockEntity.getPrice()), 6.00);

        when(rankingService.findAllStocksPopularityRanking(any(SearchVo.class))).thenReturn(Flux.just(dto));

        // When
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/stocks/rankings/popularity")
                        .queryParam("page", searchVo.getPage())
                        .queryParam("size", searchVo.getSize())
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.result.length()").isEqualTo(1)
                .jsonPath("$.result[0].id").isEqualTo(dto.getId())
                .jsonPath("$.result[0].code").isEqualTo(dto.getCode())
                .jsonPath("$.result[0].name").isEqualTo(dto.getName())
                .jsonPath("$.result[0].price").isEqualTo(dto.getPrice())
                .jsonPath("$.result[0].priceDeltaPercentage").isEqualTo(dto.getPriceDeltaPercentage());

        // Then
        verify(rankingService).findAllStocksPopularityRanking(any(SearchVo.class));
    }

    @Test
    @DisplayName("주식 인기순 증가 성공")
    void increasePopularityRankSuccess() {
        // Given
        Long stockId = 42L;
        String stockCode = "377300";
        StockIncPopularityDto stockIncPopularityDto = new StockIncPopularityDto(stockId, stockCode);

        when(rankingService.increasePopularityRank(stockId, stockCode)).thenReturn(Mono.just(500.0));

        // When
        webTestClient.put()
                .uri("/stocks/rankings/popularity/increase")
                .body(Mono.just(stockIncPopularityDto), StockIncPopularityDto.class)
                .exchange()
                .expectStatus().isOk();

        // Then
        verify(rankingService).increasePopularityRank(stockId, stockCode);
    }

    @Test
    @DisplayName("주가 상승 순위 조회 성공")
    void findAllStocksPriceDeltaRankingSuccess() {
        // Given
        SearchVo searchVo = new SearchVo(1, 10);
        StockEntity stockEntity = StockEntity.builder()
                .id(42L)
                .code("377300")
                .name("카카오페이")
                .price(new BigDecimal("1000"))
                .build();

        StockPriceDeltaRankApiDto dto = new StockPriceDeltaRankApiDto(stockEntity.getId(), stockEntity.getCode(), stockEntity.getName(), FormatUtil.formatPriceToKoreanWon(stockEntity.getPrice()), 6.00);

        when(rankingService.findAllStocksPriceDeltaRanking(any(SearchVo.class), anyString())).thenReturn(Flux.just(dto));

        // When
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/stocks/rankings/price-delta")
                        .queryParam("page", searchVo.getPage())
                        .queryParam("size", searchVo.getSize())
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.result.length()").isEqualTo(1)
                .jsonPath("$.result[0].id").isEqualTo(dto.getId())
                .jsonPath("$.result[0].code").isEqualTo(dto.getCode())
                .jsonPath("$.result[0].name").isEqualTo(dto.getName())
                .jsonPath("$.result[0].price").isEqualTo(dto.getPrice())
                .jsonPath("$.result[0].priceDeltaPercentage").isEqualTo(dto.getPriceDeltaPercentage());

        // Then
        verify(rankingService).findAllStocksPriceDeltaRanking(any(SearchVo.class), anyString());
    }
}
