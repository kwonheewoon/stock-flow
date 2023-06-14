package io.khw.ranking.ranking.router;

import io.khw.ranking.ranking.handler.RankingHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
@RequiredArgsConstructor
public class RankingRouter {

    @Bean
    public RouterFunction<ServerResponse> rankingRoutes(RankingHandler rankingHandler){
        return RouterFunctions.route()
                .GET("/stocks/rankings/volume", rankingHandler::findAllStocksVolumeRanking)
                .POST("/stocks/rankings/volume/increase", rankingHandler::increaseVolumeRank)
                .GET("/stocks/rankings/popularity", rankingHandler::findAllStocksPopularityRanking)
                .POST("/stocks/rankings/popularity/increase", rankingHandler::increasePopularityRank)
                .GET("/stocks/rankings/price-delta", rankingHandler::findAllStocksPriceDeltaRanking)
                .POST("/stocks/rankings/price-delta", rankingHandler::updatePriceDelta)
                .build();
    }
}
