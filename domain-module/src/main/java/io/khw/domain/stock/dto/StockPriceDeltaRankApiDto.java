package io.khw.domain.stock.dto;import io.khw.common.util.FormatUtil;import lombok.AllArgsConstructor;import lombok.Getter;import java.math.BigDecimal;import java.time.LocalDateTime;@AllArgsConstructor@Getterpublic class StockPriceDeltaRankApiDto {    private Long id;    private String code;    private String name;    private String price;    private double priceDeltaPercentage;    private LocalDateTime createdAt;    private LocalDateTime updatedAt;    public void setPriceDeltaPercentage(double percentage){        this.priceDeltaPercentage = percentage;    }    public void formatPrice(BigDecimal price){        this.price = FormatUtil.formatPriceToKoreanWon(price);    }}