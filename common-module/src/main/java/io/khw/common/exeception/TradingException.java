package io.khw.common.exeception;import io.khw.common.constants.ApiResponseCode;import org.springframework.http.HttpStatus;public class TradingException extends RuntimeException {    private final ApiResponseCode responseCode;    public TradingException(HttpStatus httpStatus) {        this.responseCode = ApiResponseCode.getCode(String.valueOf(httpStatus.value()), true);    }    public TradingException(ApiResponseCode apiResponseCode){        this.responseCode = apiResponseCode;    }    public String getCode() {        return responseCode.getCode();    }    public String getMessage() {        return responseCode.getMessage();    }}