package io.khw.search.blogsearch.handler;

import io.khw.domain.blogsearch.vo.SearchVo;
import io.khw.domain.common.dto.ResponseApiDto;
import io.khw.domain.popularsearchkeyword.dto.PopularSearchKeywordApiDto;
import io.khw.search.blogsearch.service.BlogSearchService;
import io.khw.search.popularsearchkeyword.service.PopularSearchKeywordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;

@Component
@RequiredArgsConstructor
public class BlogSearchHandler {

    private final BlogSearchService blogSearchService;

    private final PopularSearchKeywordService popularSearchKeywordService;

    public Mono<ServerResponse> blogSearch(ServerRequest serverRequest){

        String query = serverRequest.queryParam("query").orElse(null);
        String sort = serverRequest.queryParam("sort").orElse(null);
        int page = serverRequest.queryParam("page").map(Integer::parseInt).orElse(1);
        int size = serverRequest.queryParam("size").map(Integer::parseInt).orElse(10);

        // 필수 파라미터인 'query'가 없는 경우, 잘못된 요청으로 간주하고 badRequest 응답을 반환합니다.
        if (query == null) {
            return ServerResponse.badRequest().bodyValue("Query parameter 'query' is required.");
        }

        return blogSearchService.search(SearchVo.create(query, sort, page, size))
                .flatMap(search -> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue(search))
                .switchIfEmpty(ServerResponse.noContent().build());
    }

    public Mono<ServerResponse> searchBlogTopKeywords(ServerRequest serverRequest){

        Flux<PopularSearchKeywordApiDto> searchKeywordApiDtos = popularSearchKeywordService.getTopKeyWords();
        return searchKeywordApiDtos.collectList()
                .flatMap(documents -> {
                    if (documents.isEmpty()) {
                        return ServerResponse.ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(new ResponseApiDto<>(new ArrayList<>(), 0));
                    } else {
                        return ServerResponse.ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(new ResponseApiDto<>(documents, documents.size()));
                    }
                });
    }
}
