package io.khw.common.exeception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.khw.common.constants.ApiResponseCode;
import io.khw.common.response.ErrCommonResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.MediaType;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;


@Order(-2)
@Configuration
@RequiredArgsConstructor
public class GlobalWebExceptionHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> handle(ServerWebExchange serverWebExchange, Throwable throwable) {
        return handleException(serverWebExchange, throwable);
    }

    private Mono<Void> handleException(ServerWebExchange serverWebExchange, Throwable throwable) {
        ErrCommonResponse errorResponse = null;
        DataBuffer dataBuffer = null;

        DataBufferFactory bufferFactory = serverWebExchange.getResponse().bufferFactory();
        serverWebExchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        if(throwable instanceof RankingException){
            RankingException ex = (RankingException) throwable;
            errorResponse = new ErrCommonResponse(ex.getCode(), ex.getMessage());
        }
        else if(throwable instanceof ResponseStatusException){
            ResponseStatusException ex = (ResponseStatusException) throwable;

            String errorMessage = ex.getReason();

            String defaultMessage = null;
            if (errorMessage != null) {
                int startIndex = errorMessage.lastIndexOf("default message [");
                int endIndex = errorMessage.lastIndexOf("]]");

                if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
                    defaultMessage = errorMessage.substring(startIndex + 17, endIndex);
                }
            }

            if (defaultMessage != null) {
                errorResponse = new ErrCommonResponse(ApiResponseCode.BAD_REQUEST.getCode(), defaultMessage);
            } else {
                errorResponse = new ErrCommonResponse(ApiResponseCode.BAD_REQUEST.getCode(), "Invalid request");
            }
        }
        else{
            errorResponse = new ErrCommonResponse(ApiResponseCode.SERVER_ERROR);
        }

        try{
            dataBuffer = bufferFactory.wrap(objectMapper.writeValueAsBytes(errorResponse));
        }catch (JsonProcessingException e) {
            dataBuffer = bufferFactory.wrap("JSON Format Error".getBytes());
        }

        return serverWebExchange.getResponse().writeWith(Mono.just(dataBuffer));
    }
}
