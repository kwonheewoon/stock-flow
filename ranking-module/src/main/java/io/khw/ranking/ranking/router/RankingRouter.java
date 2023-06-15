package io.khw.ranking.ranking.router;

import io.khw.common.response.CommonResponse;
import io.khw.domain.stock.dto.StockIncPopularityDto;
import io.khw.domain.stock.dto.StockIncVolumeDto;
import io.khw.domain.stock.dto.StockPriceVolumeDto;
import io.khw.ranking.ranking.handler.RankingHandler;
import io.khw.ranking.ranking.service.RankingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
@RequiredArgsConstructor
public class RankingRouter {

    @Bean
    @RouterOperations(
            {
                    @RouterOperation(path = "/stocks/{stockId}/{stockCode}"
                            , produces = {MediaType.APPLICATION_JSON_VALUE}, method = RequestMethod.GET, beanClass = RankingHandler.class, beanMethod = "findStockPriceDetailAndUpdatePopularity",
                            operation = @Operation(operationId = "findStockPriceDetailAndUpdatePopularity", responses = {
                                    @ApiResponse(responseCode = "FIND_ALL_VOLUME_RANK_OK", description = "종목 거래량 순위 조회 성공", content = @Content(schema = @Schema(implementation = CommonResponse.class))),
                                    @ApiResponse(responseCode = "STOCK_NOT_FOUND", description = "존재하지 않는 종목입니다."),
                                    },
                                    parameters = {
                                            @Parameter(in = ParameterIn.PATH, name = "stockId", description = "주가 아이디", required = true),
                                            @Parameter(in = ParameterIn.PATH, name = "stockCode", description = "주가 코드", required = true)
                                    })
                    ),
                    @RouterOperation(path = "/stocks/rankings/{stockId}/{stockCode}"
                            , produces = {MediaType.APPLICATION_JSON_VALUE}, method = RequestMethod.POST, beanClass = RankingHandler.class, beanMethod = "updateStockPriceAndVolumeRank",
                            operation = @Operation(operationId = "updateStockPriceAndVolumeRank", responses = {
                                    @ApiResponse(responseCode = "STOCK_TRADE_OK", description = "주식 거래 완료", content = @Content(schema = @Schema(implementation = CommonResponse.class))),
                                    @ApiResponse(responseCode = "STOCK_NOT_FOUND", description = "존재하지 않는 종목입니다."),
                            },
                                    parameters = {
                                            @Parameter(in = ParameterIn.PATH, name = "stockId", description = "주가 아이디", required = true),
                                            @Parameter(in = ParameterIn.PATH, name = "stockCode", description = "주가 코드", required = true)
                                    },
                                    requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = StockPriceVolumeDto.class))))
                    ),
                    @RouterOperation(path = "/stocks/rankings/volume"
                            , produces = {MediaType.APPLICATION_JSON_VALUE}, method = RequestMethod.GET, beanClass = RankingHandler.class, beanMethod = "findAllStocksVolumeRanking",
                            operation = @Operation(operationId = "findAllStocksVolumeRanking", responses = {
                                    @ApiResponse(responseCode = "FIND_ALL_VOLUME_RANK_OK", description = "종목 거래량 순위 조회 성공", content = @Content(schema = @Schema(implementation = CommonResponse.class))),
                                    @ApiResponse(responseCode = "SERVER_ERROR", description = "서버에 장애가 발생하였습니다."),
                            },
                                    parameters = {
                                            @Parameter(in = ParameterIn.QUERY, name = "page", description = "페이지 번호", required = true),
                                            @Parameter(in = ParameterIn.QUERY, name = "size", description = "페이지 표출 사이즈", required = true)
                                    })
                    ),
                    @RouterOperation(path = "/stocks/rankings/popularity"
                            , produces = {MediaType.APPLICATION_JSON_VALUE}, method = RequestMethod.GET, beanClass = RankingHandler.class, beanMethod = "findAllStocksPopularityRanking",
                            operation = @Operation(operationId = "findAllStocksPopularityRanking", responses = {
                                    @ApiResponse(responseCode = "FIND_ALL_POPULARITY_RANK_OK", description = "종목 인기 순위 조회 성공", content = @Content(schema = @Schema(implementation = CommonResponse.class))),
                                    @ApiResponse(responseCode = "SERVER_ERROR", description = "서버에 장애가 발생하였습니다."),
                            },
                                    parameters = {
                                            @Parameter(in = ParameterIn.QUERY, name = "page", description = "페이지 번호", required = true),
                                            @Parameter(in = ParameterIn.QUERY, name = "size", description = "페이지 표출 사이즈", required = true)
                                    })
                    ),
                    @RouterOperation(path = "/stocks/rankings/price-delta"
                            , produces = {MediaType.APPLICATION_JSON_VALUE}, method = RequestMethod.GET, beanClass = RankingHandler.class, beanMethod = "findAllStocksPriceDeltaRanking",
                            operation = @Operation(operationId = "findAllStocksPriceDeltaRanking", responses = {
                                    @ApiResponse(responseCode = "FIND_ALL_PRICE_DELTA_RANK_OK", description = "종목 상승,하락가 순위 조회 성공", content = @Content(schema = @Schema(implementation = CommonResponse.class))),
                                    @ApiResponse(responseCode = "SERVER_ERROR", description = "서버에 장애가 발생하였습니다."),
                            },
                                    parameters = {
                                            @Parameter(in = ParameterIn.QUERY, name = "page", description = "페이지 번호", required = true),
                                            @Parameter(in = ParameterIn.QUERY, name = "size", description = "페이지 표출 사이즈", required = true)
                                    })
                    ),
                    @RouterOperation(path = "/stocks/rankings/volume/increase"
                            , produces = {MediaType.APPLICATION_JSON_VALUE}, method = RequestMethod.PUT, beanClass = RankingHandler.class, beanMethod = "increaseVolumeRank",
                            operation = @Operation(operationId = "increaseVolumeRank", responses = {
                                    @ApiResponse(responseCode = "INCREASE_VOLUME_RANK_OK", description = "종목 거래량 증가 성공", content = @Content(schema = @Schema(implementation = CommonResponse.class))),
                                    @ApiResponse(responseCode = "FIND_ALL_VOLUME_RANK_OK", description = "종목 거래량 순위 조회 성공"),
                            },
                                    requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = StockIncVolumeDto.class))))
                    ),
                    @RouterOperation(path = "/stocks/rankings/popularity/increase"
                            , produces = {MediaType.APPLICATION_JSON_VALUE}, method = RequestMethod.PUT, beanClass = RankingHandler.class, beanMethod = "increasePopularityRank",
                            operation = @Operation(operationId = "increasePopularityRank", responses = {
                                    @ApiResponse(responseCode = "INCREASE_POPULARITY_RANK_OK", description = "종목 인기 증가 성공", content = @Content(schema = @Schema(implementation = CommonResponse.class))),
                                    @ApiResponse(responseCode = "FIND_ALL_POPULARITY_RANK_OK", description = "종목 인기 순위 조회 성공"),
                            },
                                    requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = StockIncPopularityDto.class))))
                    )
            })
    public RouterFunction<ServerResponse> rankingRoutes(RankingHandler rankingHandler){
        return RouterFunctions.route()
                .PUT("/stocks/rankings/price-delta", rankingHandler::updatePriceDelta)
                .PUT("/stocks/rankings/volume/increase", rankingHandler::increaseVolumeRank)
                .PUT("/stocks/rankings/popularity/increase", rankingHandler::increasePopularityRank)
                .GET("/stocks/rankings/volume", rankingHandler::findAllStocksVolumeRanking)
                .GET("/stocks/rankings/popularity", rankingHandler::findAllStocksPopularityRanking)
                .GET("/stocks/rankings/price-delta", rankingHandler::findAllStocksPriceDeltaRanking)
                .POST("/stocks/rankings/{stockId}/{stockCode}", rankingHandler::updateStockPriceAndVolumeRank)
                .GET("/stocks/{stockId}/{stockCode}", rankingHandler::findStockPriceDetailAndUpdatePopularity)
                .build();
    }
}
