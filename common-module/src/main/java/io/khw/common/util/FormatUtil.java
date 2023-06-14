package io.khw.common.util;

import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

@UtilityClass
public class FormatUtil {

    public static String formatPriceToKoreanWon(BigDecimal price) {
        NumberFormat format = NumberFormat.getCurrencyInstance(Locale.KOREA);
        String formattedPrice = format.format(price.doubleValue());
        return formattedPrice;
    }
}
