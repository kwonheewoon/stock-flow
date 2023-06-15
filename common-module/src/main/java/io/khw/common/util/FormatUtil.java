package io.khw.common.util;

import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.text.DecimalFormat;


@UtilityClass
public class FormatUtil {

    /**
     * BigDecimal 자료형의 가격 데이터 -> #,###원 포맷 메소드
     *
     * @param price 가격
     *
     * @return String
     */
    public static String formatPriceToKoreanWon(BigDecimal price) {
        DecimalFormat formatter = new DecimalFormat("#,###원");
        String formattedPrice = formatter.format(price);
        return formattedPrice;
    }
}
