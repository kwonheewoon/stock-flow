package io.khw.common.config;import io.netty.channel.ChannelOption;import io.netty.handler.timeout.ReadTimeoutHandler;import io.netty.handler.timeout.WriteTimeoutHandler;import org.springframework.context.annotation.Bean;import org.springframework.context.annotation.Configuration;import org.springframework.http.client.reactive.ReactorClientHttpConnector;import org.springframework.web.reactive.function.client.WebClient;import reactor.netty.http.client.HttpClient;import reactor.netty.resources.ConnectionProvider;import java.time.Duration;import java.util.concurrent.TimeUnit;@Configurationpublic class WebClientConfig {    @Bean    public WebClient.Builder webClientBuilder() {        int maxConnections = 1000; // 최대 연결 수        int maxIdleTime = 300; // 최대 유휴 시간 (ms)        ConnectionProvider connectionProvider = ConnectionProvider.builder("custom-connection-provider")                .maxConnections(maxConnections)                .maxIdleTime(Duration.ofMillis(maxIdleTime))                .build();        HttpClient httpClient = HttpClient.create(connectionProvider)                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000) // 커넥션 타임아웃 (ms)                .doOnConnected(connection ->                        connection.addHandlerLast(new ReadTimeoutHandler(5000, TimeUnit.MILLISECONDS)) // 읽기 타임아웃 (ms)                                .addHandlerLast(new WriteTimeoutHandler(5000, TimeUnit.MILLISECONDS)) // 쓰기 타임아웃 (ms)                );        return WebClient.builder()                .clientConnector(new ReactorClientHttpConnector(httpClient));    }}