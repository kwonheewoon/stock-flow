package io.khw.common.response;import io.khw.common.constants.ApiResponseCode;import lombok.Getter;@Getterpublic class CommonResponse<T>{    private String message;    private T result;    private String code;    public CommonResponse(String code, String message, T parameter) {        this.code = code;        this.message = message;        this.result = parameter;    }    public CommonResponse(String message, T parameter) {        this.message = message;        this.result = parameter;    }    public CommonResponse(String code, String message) {        this.code = code;        this.message = message;    }    public CommonResponse(ApiResponseCode apiResponseCode, T parameter) {        this.code = apiResponseCode.getCode();        this.message = apiResponseCode.getMessage();        this.result = parameter;    }}