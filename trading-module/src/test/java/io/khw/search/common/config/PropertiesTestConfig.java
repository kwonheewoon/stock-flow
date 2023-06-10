package io.khw.search.common.config;import lombok.Getter;import lombok.Setter;import java.util.Map;@Getter@Setterpublic class PropertiesTestConfig {    private String main = "naver";    private ApiProperties kakao = new ApiProperties();    private ApiProperties naver = new ApiProperties();    // Getter 및 Setter 메소드    @Getter    @Setter    public static class ApiProperties {        private String url = "http://localhost:8080";        private String path = "/v2/search/blog";        private Map<String, String> headers = Map.of("KakaoAK", "298b3a98921581d3ae42613a4003d641");        private String queryParamKey = "query";        private String sortParamKey = "sort";        private String pageParamKey = "page";        private String sizeParamKey = "size";    }}