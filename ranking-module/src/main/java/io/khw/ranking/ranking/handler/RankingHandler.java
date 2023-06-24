package io.khw.ranking.ranking.handler;

import io.khw.common.constants.ApiResponseCode;
import io.khw.common.enums.StockOrderEnum;
import io.khw.common.response.CommonResponse;
import io.khw.common.validate.CommonValidate;
import io.khw.domain.common.vo.SearchVo;
import io.khw.domain.stock.dto.StockIncPopularityDto;
import io.khw.domain.stock.dto.StockIncVolumeDto;
import io.khw.domain.stock.dto.StockPriceVolumeDto;
import io.khw.domain.stock.dto.StockUpdatePriceDeltaDto;
import io.khw.ranking.ranking.service.RankingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;


@Component
@RequiredArgsConstructor
public class RankingHandler {

    private final RankingService rankingService;

    private final CommonValidate validate;

    public Mono<ServerResponse> findStockPriceDetailAndUpdatePopularity(ServerRequest serverRequest){

        Long stockId = Long.valueOf(serverRequest.pathVariable("stockId"));
        String stockCode = serverRequest.pathVariable("stockCode");



        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(rankingService.findStockPriceDetailAndUpdatePopularity(stockId, stockCode)
                        .map(list -> new CommonResponse<>(ApiResponseCode.FIND_ALL_VOLUME_RANK_OK,list)), CommonResponse.class);

    }

    public Mono<ServerResponse> updateStockPriceAndVolumeRank(ServerRequest request) {
        Long stockId = Long.valueOf(request.pathVariable("stockId"));
        String stockCode = request.pathVariable("stockCode");

        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(request.bodyToMono(StockPriceVolumeDto.class)
                        .doOnNext(stockPriceVolumeDto -> validate.validate(stockPriceVolumeDto))
                        .flatMap(requestBody -> rankingService.updateStockPriceAndVolumeRank(stockId, stockCode,
                                requestBody.getBuyPrice(), requestBody.getTradeVolume()))
                        .map(result -> new CommonResponse<>(ApiResponseCode.STOCK_TRADE_OK)), CommonResponse.class);

    }

    public Mono<ServerResponse> findAllStocksVolumeRanking(ServerRequest serverRequest){

        int page = Math.max(1, Integer.parseInt(serverRequest.queryParam("page").orElse("1")));
        int size = Math.max(10, Integer.parseInt(serverRequest.queryParam("size").orElse("10")));

        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(rankingService.findAllStocksVolumeRanking(new SearchVo(page, size)).collectList()
                                .map(list -> new CommonResponse<>(ApiResponseCode.FIND_ALL_VOLUME_RANK_OK,list)), CommonResponse.class);

    }

    public Mono<ServerResponse> increaseVolumeRank(ServerRequest serverRequest){

        return serverRequest.bodyToMono(StockIncVolumeDto.class)
                        .flatMap(stockIncVolumeDto ->
                            ServerResponse.ok()
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .body(rankingService.increaseVolumeRank(stockIncVolumeDto.getStockId(), stockIncVolumeDto.getStockCode(), stockIncVolumeDto.getTradeVolume())
                                            .map(volume -> new CommonResponse<>(ApiResponseCode.INCREASE_VOLUME_RANK_OK,volume)), CommonResponse.class)
                        );

    }

    public Mono<ServerResponse> findAllStocksPopularityRanking(ServerRequest serverRequest){

        int page = Math.max(1, Integer.parseInt(serverRequest.queryParam("page").orElse("1")));
        int size = Math.max(10, Integer.parseInt(serverRequest.queryParam("size").orElse("10")));

        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(rankingService.findAllStocksPopularityRanking(new SearchVo(page, size)).collectList()
                        .map(list -> new CommonResponse<>(ApiResponseCode.FIND_ALL_POPULARITY_RANK_OK, list)), CommonResponse.class);

    }

    public Mono<ServerResponse> increasePopularityRank(ServerRequest serverRequest){

        return serverRequest.bodyToMono(StockIncPopularityDto.class)
                .flatMap(stockIncVolumeDto ->
                        ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(rankingService.increasePopularityRank(stockIncVolumeDto.getStockId(), stockIncVolumeDto.getStockCode())
                            .map(popularity -> new CommonResponse<>(ApiResponseCode.INCREASE_POPULARITY_RANK_OK,popularity)), CommonResponse.class));

    }

    public Mono<ServerResponse> findAllStocksPriceDeltaRanking(ServerRequest serverRequest){

        int page = Math.max(1, Integer.parseInt(serverRequest.queryParam("page").orElse("1")));
        int size = Math.max(10, Integer.parseInt(serverRequest.queryParam("size").orElse("10")));
        String orderType = serverRequest.queryParam("orderType").orElse(StockOrderEnum.INC.name());

        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(rankingService.findAllStocksPriceDeltaRanking(new SearchVo(page, size), orderType).collectList()
                        .map(list -> new CommonResponse<>(ApiResponseCode.FIND_ALL_PRICE_DELTA_RANK_OK, list)), CommonResponse.class);

    }

    public Mono<ServerResponse> updatePriceDelta(ServerRequest serverRequest){

        return serverRequest.bodyToMono(StockUpdatePriceDeltaDto.class)
                .flatMap(stockUpdatePriceDeltaDto -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(rankingService.updatePriceDelta(stockUpdatePriceDeltaDto.getStockId(), stockUpdatePriceDeltaDto.getStockCode(), stockUpdatePriceDeltaDto.getBuyPrice(), stockUpdatePriceDeltaDto.getBuyPrice())
                                .map(priceDelta -> new CommonResponse<>(ApiResponseCode.UPDATE_PRICE_DELTA_RANK_OK,priceDelta)), CommonResponse.class));

    }
}
