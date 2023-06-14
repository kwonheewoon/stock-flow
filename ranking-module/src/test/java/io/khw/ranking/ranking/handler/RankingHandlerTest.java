package io.khw.ranking.ranking.handler;

import io.khw.common.constants.ApiResponseCode;
import io.khw.common.exeception.RankingException;
import io.khw.common.util.FormatUtil;
import io.khw.domain.common.vo.SearchVo;
import io.khw.domain.stock.dto.StockIncPopularityDto;
import io.khw.domain.stock.dto.StockIncVolumeDto;
import io.khw.domain.stock.dto.StockPriceDeltaRankApiDto;
import io.khw.domain.stock.dto.StockUpdatePriceDeltaDto;
import io.khw.domain.stock.entity.StockEntity;
import io.khw.ranking.ranking.service.RankingService;
import org.junit.jupiter.api.BeforeEach;
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
import java.util.Map;

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
                .GET("/stocks/rankings/volume", rankingHandler::findAllStocksVolumeRanking)
                .POST("/stocks/rankings/volume/increase", rankingHandler::increaseVolumeRank)
                .GET("/stocks/rankings/popularity", rankingHandler::findAllStocksPopularityRanking)
                .POST("/stocks/rankings/popularity/increase", rankingHandler::increasePopularityRank)
                .GET("/stocks/rankings/price-delta", rankingHandler::findAllStocksPriceDeltaRanking)
                .POST("/stocks/rankings/price-delta", rankingHandler::updatePriceDelta)
                .build();

        webTestClient = WebTestClient.bindToRouterFunction(routerFunction).build();
    }

    @Test
    void findAllStocksVolumeRankingSuccess() {
        // Given
        SearchVo searchVo = new SearchVo(1, 10);
        StockEntity stockEntity = StockEntity.builder()
                .id(42L)
                .code("377300")
                .name("카카오페이")
                .price(new BigDecimal("1000"))
                .build();

        StockPriceDeltaRankApiDto dto = new StockPriceDeltaRankApiDto(stockEntity.getId(), stockEntity.getCode(), stockEntity.getName(), FormatUtil.formatPriceToKoreanWon(stockEntity.getPrice()), 6.00, stockEntity.getCreatedAt(), stockEntity.getUpdatedAt());

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
    void findAllStocksVolumeRanking_When_RankingException() {
        // Given
        SearchVo searchVo = new SearchVo(1, 10);

        when(rankingService.findAllStocksVolumeRanking(any(SearchVo.class))).thenThrow(new RankingException(ApiResponseCode.STOCK_NOT_FOUND));

        // When & Then
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/stocks/rankings/volume")
                        .queryParam("page", searchVo.getPage())
                        .queryParam("size", searchVo.getSize())
                        .build())
                .exchange()
                .expectStatus().is5xxServerError(); // Assuming that the RankingException maps to a 4xx HTTP status

        verify(rankingService).findAllStocksVolumeRanking(any(SearchVo.class));
    }

    @Test
    void increaseVolumeRankSuccess() {
        // Given
        Long stockId = 42L;
        String stockCode = "377300";
        double tradeVolume = 1000;
        StockIncVolumeDto stockIncVolumeDto = new StockIncVolumeDto(stockId, stockCode, tradeVolume);

        when(rankingService.increaseVolumeRank(stockId, stockCode, tradeVolume)).thenReturn(Mono.just(500.0));

        // When & Then
        webTestClient.post()  // Assuming this is a POST request. If not, change this to GET or appropriate HTTP method.
                .uri("/stocks/rankings/volume/increase") // Assuming this is the endpoint for the method.
                .body(Mono.just(stockIncVolumeDto), StockIncVolumeDto.class)
                .exchange()
                .expectStatus().isOk();

        verify(rankingService).increaseVolumeRank(stockId, stockCode, tradeVolume);
    }


    @Test
    void findAllStocksPopularityRankingSuccess() {
        // Given
        SearchVo searchVo = new SearchVo(1, 10);
        StockEntity stockEntity = StockEntity.builder()
                .id(42L)
                .code("377300")
                .name("카카오페이")
                .price(new BigDecimal("1000"))
                .build();

        StockPriceDeltaRankApiDto dto = new StockPriceDeltaRankApiDto(stockEntity.getId(), stockEntity.getCode(), stockEntity.getName(), FormatUtil.formatPriceToKoreanWon(stockEntity.getPrice()), 6.00, stockEntity.getCreatedAt(), stockEntity.getUpdatedAt());

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
    void increasePopularityRankSuccess() {
        // Given
        Long stockId = 42L;
        String stockCode = "377300";
        StockIncPopularityDto stockIncPopularityDto = new StockIncPopularityDto(stockId, stockCode);

        when(rankingService.increasePopularityRank(stockId, stockCode)).thenReturn(Mono.just(500.0));

        // When & Then
        webTestClient.post()  // Assuming this is a POST request. If not, change this to GET or appropriate HTTP method.
                .uri("/stocks/rankings/popularity/increase") // Assuming this is the endpoint for the method.
                .body(Mono.just(stockIncPopularityDto), StockIncPopularityDto.class)
                .exchange()
                .expectStatus().isOk();

        verify(rankingService).increasePopularityRank(stockId, stockCode);
    }

    @Test
    void findAllStocksPriceDeltaRankingSuccess() {
        // Given
        SearchVo searchVo = new SearchVo(1, 10);
        StockEntity stockEntity = StockEntity.builder()
                .id(42L)
                .code("377300")
                .name("카카오페이")
                .price(new BigDecimal("1000"))
                .build();

        StockPriceDeltaRankApiDto dto = new StockPriceDeltaRankApiDto(stockEntity.getId(), stockEntity.getCode(), stockEntity.getName(), FormatUtil.formatPriceToKoreanWon(stockEntity.getPrice()), 6.00, stockEntity.getCreatedAt(), stockEntity.getUpdatedAt());

        when(rankingService.findAllStocksPriceDeltaRanking(any(SearchVo.class))).thenReturn(Flux.just(dto));

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
        verify(rankingService).findAllStocksPriceDeltaRanking(any(SearchVo.class));
    }

    @Test
    void updatePriceDeltaSuccess() {
        // Given
        Long stockId = 42L;
        String stockCode = "377300";
        BigDecimal buyPrice = new BigDecimal("64000");
        StockUpdatePriceDeltaDto stockUpdatePriceDeltaDto = new StockUpdatePriceDeltaDto(stockId, stockCode, buyPrice);

        when(rankingService.updatePriceDelta(stockId, stockCode, buyPrice)).thenReturn(Mono.just(true));

        // When & Then
        webTestClient.post()  // Assuming this is a POST request. If not, change this to GET or appropriate HTTP method.
                .uri("/stocks/rankings/price-delta") // Assuming this is the endpoint for the method.
                .body(Mono.just(stockUpdatePriceDeltaDto), StockUpdatePriceDeltaDto.class)
                .exchange()
                .expectStatus().isOk();

        verify(rankingService).updatePriceDelta(stockId, stockCode, buyPrice);
    }
}
