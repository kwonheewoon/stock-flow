package io.khw.domain.popularsearchkeyword.repository_r2dbc;

import io.khw.domain.popularsearchkeyword.entity.PopularSearchKeywordEntity;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PopularSearchKeywordReactRepository extends R2dbcRepository<PopularSearchKeywordEntity, Long> {
    Mono<PopularSearchKeywordEntity> findByKeyword(String keyword);

    Flux<PopularSearchKeywordEntity> findTop10ByOrderBySearchVolumeDesc();


    @Modifying
    @Query("UPDATE popular_search_keyword SET search_volume = search_volume + 1 WHERE keyword = :keyword")
    Mono<Integer> incrementSearchVolumeByKeyword(String keyword);

}
