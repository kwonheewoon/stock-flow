package io.khw.common.exeception;import io.khw.common.constants.ApiResponseCode;import org.springframework.http.HttpStatus;public class ApiException extends RuntimeException {    private final ApiResponseCode responseCode;    public ApiException(HttpStatus httpStatus) {        this.responseCode = ApiResponseCode.getCode(String.valueOf(httpStatus.value()), true);    }    public ApiException(ApiResponseCode apiResponseCode){        this.responseCode = apiResponseCode;    }    public String getCode() {        return responseCode.getCode();    }    public String getMessage() {        return responseCode.getMessage();    }    public HttpStatus getStatus() {        return responseCode.getHttpStatus();    }}