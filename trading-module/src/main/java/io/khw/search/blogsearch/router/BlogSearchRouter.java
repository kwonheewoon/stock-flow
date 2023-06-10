package io.khw.search.blogsearch.router;

import io.khw.domain.blogsearch.dto.CommonApiResponseDto;
import io.khw.domain.popularsearchkeyword.dto.PopularSearchKeywordApiDto;
import io.khw.search.blogsearch.handler.BlogSearchHandler;
import io.khw.search.blogsearch.service.impl.KakaoBlogSearchService;
import io.khw.search.popularsearchkeyword.service.PopularSearchKeywordService;
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
public class BlogSearchRouter {

    @Bean
    @RouterOperations(
            {
                    @RouterOperation(path = "/search/blog"
                            , produces = {MediaType.APPLICATION_JSON_VALUE}, method = RequestMethod.GET, beanClass = KakaoBlogSearchService.class, beanMethod = "search",
                            operation = @Operation(operationId = "blogSearch", responses = {
                                    @ApiResponse(responseCode = "200", description = "successful operation", content = @Content(schema = @Schema(implementation = CommonApiResponseDto.class))),
                                    @ApiResponse(responseCode = "400", description = "Invalid Employee ID supplied"),
                                    @ApiResponse(responseCode = "404", description = "Employee not found")},
                                    parameters = {
                                            @Parameter(in = ParameterIn.QUERY, name = "query", description = "Search keyword", required = true),
                                            @Parameter(in = ParameterIn.QUERY, name = "sort", description = "Sort option"),
                                            @Parameter(in = ParameterIn.QUERY, name = "page", description = "Page number"),
                                            @Parameter(in = ParameterIn.QUERY, name = "size", description = "Page size")
                                    },
                                    requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = CommonApiResponseDto.class))))
                    ),
                    @RouterOperation(path = "/search/blog/top-keywords", produces = {
                            MediaType.APPLICATION_JSON_VALUE}, method = RequestMethod.POST, beanClass = PopularSearchKeywordService.class, beanMethod = "getTopKeyWords",
                            operation = @Operation(operationId = "insertEmployee", responses = {
                                    @ApiResponse(responseCode = "200", description = "successful operation", content = @Content(schema = @Schema(implementation = PopularSearchKeywordApiDto.class))),
                                    @ApiResponse(responseCode = "400", description = "Invalid Employee details supplied")}
                                    , requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = PopularSearchKeywordApiDto.class)))
                            ))

            })
    public RouterFunction<ServerResponse> blogSearchRoutes(BlogSearchHandler blogSearchHandler){
        return RouterFunctions.route()
                .GET("/search/blog", blogSearchHandler::blogSearch)
                .GET("/search/blog/top-keywords", blogSearchHandler::searchBlogTopKeywords)
                .build();
    }
}
