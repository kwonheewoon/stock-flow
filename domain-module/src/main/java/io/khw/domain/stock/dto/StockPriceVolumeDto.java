package io.khw.domain.stock.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class StockPriceVolumeDto {

    private Long stockId;

    private String stockCode;

    @NotNull(message = "구매 가격은 필수 값 입니다.")
    private BigDecimal buyPrice;

    @Min(message = "구매 수량은 1 이상의 값 이어야 합니다.", value = 1)
    private int tradeVolume;
}
