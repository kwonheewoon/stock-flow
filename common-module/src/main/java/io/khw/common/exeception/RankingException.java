package io.khw.common.exeception;import io.khw.common.constants.ApiResponseCode;import org.springframework.http.HttpStatus;public class RankingException extends RuntimeException {    private final ApiResponseCode responseCode;    public RankingException(HttpStatus httpStatus) {        this.responseCode = ApiResponseCode.getCode(String.valueOf(httpStatus.value()), true);    }    public RankingException(ApiResponseCode apiResponseCode){        this.responseCode = apiResponseCode;    }    public String getCode() {        return responseCode.getCode();    }    public String getMessage() {        return responseCode.getMessage();    }}