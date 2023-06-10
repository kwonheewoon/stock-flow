package io.khw.common.config;import io.swagger.v3.oas.models.OpenAPI;import io.swagger.v3.oas.models.info.Info;import org.springdoc.core.models.GroupedOpenApi;import org.springframework.context.annotation.Bean;import org.springframework.context.annotation.Configuration;@Configurationpublic class SwaggerConfig {    @Bean    public GroupedOpenApi publicApi() {        return GroupedOpenApi.builder()                .group("v1-definition")                .pathsToMatch("/api/**")                .build();    }    @Bean    public OpenAPI springShopOpenAPI() {        return new OpenAPI()                .info(new Info().title("블로그 검색 API")                        .description("블로그, 카카오 블로그 검색 API")                        .version("v0.0.1"));    }//    @Bean//    public OpenAPI customOpenAPI() {//        return new OpenAPI()//                .components(new Components())//                .info(new Info()//                        .title("Sample WebFlux API")//                        .description("This is a sample Spring WebFlux API with SpringDoc OpenAPI integration.")//                        .version("1.0.0")//                        .license(new License().name("Apache 2.0").url("http://springdoc.org")));//    }}