package io.khw.common.constants;import org.springframework.http.HttpStatus;import java.util.Arrays;public enum ApiResponseCode {    //OK    INCREASE_VOLUME_RANK_OK(HttpStatus.OK, "INCREASE_VOLUME_RANK_OK", "종목 거래량 증가 성공"),    INCREASE_POPULARITY_RANK_OK(HttpStatus.OK, "INCREASE_POPULARITY_RANK_OK", "종목 인기 증가 성공"),    UPDATE_INCREASING_RANK_OK(HttpStatus.OK, "UPDATE_INCREASING_RANK_OK", "종목 상승가 업데이트 성공"),    UPDATE_DECREASING_RANK_OK(HttpStatus.OK, "UPDATE_DECREASING_RANK_OK", "종목 하락가 업데이트 성공"),    //BAD_REQUEST    BAD_REQUEST(HttpStatus.BAD_REQUEST,"00400", "잘못된 파라미터 입니다. 오타가 있는지 확인해 주세요."),    STOCK_PRICE_BAD_REQUEST(HttpStatus.BAD_REQUEST,"STOCK_PRICE_BAD_REQUEST", "주식 가격이 0보다 작거나 같을수 없습니다."),    //UNAUTHORIZED    UNAUTHORIZED(HttpStatus.UNAUTHORIZED,"00401", "헤더 검증에 실패 하였습니다. 헤더 값을 확인해 주세요."),    //NOT_FOUND    NOT_FOUND(HttpStatus.NOT_FOUND,"NOT_FOUND", "잘못된 URL 입니다. 오타가 있는지 확인해 주세요."),    STOCK_NOT_FOUND(HttpStatus.NOT_FOUND,"STOCK_NOT_FOUND", "존재하지 않는 종목입니다."),    //SERVER_ERROR    SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR,"00500", "서버에 장애가 발생하였습니다. 관리자에게 문의 주세요."),    //SERVER_ERROR    CALCULATE_PRICE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR,"CALCULATE_PRICE_ERROR", "가격 연산에 오류가 발생하였습니다."),    REDIS_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR,"REDIS_SERVER_ERROR", "Redis 서버에 장애가 발생하였습니다."),    SYSTEM_ERROR(HttpStatus.INTERNAL_SERVER_ERROR,"00999", "시스템에 장애가 발생하였습니다. 관리자에게 문의 주세요."),    ;    private String code;    private String message;    private HttpStatus httpStatus;    ApiResponseCode(HttpStatus httpStatus, String code, String message) {        this.code = code;        this.message = message;        this.httpStatus = httpStatus;    }    public static ApiResponseCode getCode(String searchCode, boolean valExp) {        if(valExp) searchCode = "00" + searchCode;        String finalSearchCode = searchCode;        return Arrays.stream(ApiResponseCode.values()).filter(m -> m.code.equals(finalSearchCode)).findAny().orElse(null);    }    public static ApiResponseCode fromHttpStatus(HttpStatus searchHttpStatus) {        return Arrays.stream(ApiResponseCode.values())                .filter(m -> m.httpStatus.equals(searchHttpStatus))                .findFirst()                .orElse(SYSTEM_ERROR);    }    public String getCode() {        return code;    }    public String getMessage() {        return message;    }    public HttpStatus getHttpStatus(){        return httpStatus;    }}