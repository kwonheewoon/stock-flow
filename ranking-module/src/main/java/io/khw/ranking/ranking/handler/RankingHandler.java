package io.khw.ranking.ranking.handler;

import io.khw.common.constants.ApiResponseCode;
import io.khw.common.response.CommonResponse;
import io.khw.domain.common.vo.SearchVo;
import io.khw.domain.stock.dto.StockIncPopularityDto;
import io.khw.domain.stock.dto.StockIncVolumeDto;
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

    public Mono<ServerResponse> findAllStocksVolumeRanking(ServerRequest serverRequest){

        int page = Integer.parseInt(serverRequest.queryParam("page").orElse("1"));
        int size = Integer.parseInt(serverRequest.queryParam("size").orElse("10"));

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

        int page = Integer.parseInt(serverRequest.queryParam("page").orElse("1"));
        int size = Integer.parseInt(serverRequest.queryParam("size").orElse("10"));

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

        int page = Integer.parseInt(serverRequest.queryParam("page").orElse("1"));
        int size = Integer.parseInt(serverRequest.queryParam("size").orElse("10"));

        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(rankingService.findAllStocksPriceDeltaRanking(new SearchVo(page, size)).collectList()
                        .map(list -> new CommonResponse<>(ApiResponseCode.FIND_ALL_PRICE_DELTA_RANK_OK, list)), CommonResponse.class);

    }

    public Mono<ServerResponse> updatePriceDelta(ServerRequest serverRequest){

        return serverRequest.bodyToMono(StockUpdatePriceDeltaDto.class)
                .flatMap(stockUpdatePriceDeltaDto -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(rankingService.updatePriceDelta(stockUpdatePriceDeltaDto.getStockId(), stockUpdatePriceDeltaDto.getStockCode(), stockUpdatePriceDeltaDto.getBuyPrice())
                                .map(priceDelta -> new CommonResponse<>(ApiResponseCode.UPDATE_PRICE_DELTA_RANK_OK,priceDelta)), CommonResponse.class));

    }
}
