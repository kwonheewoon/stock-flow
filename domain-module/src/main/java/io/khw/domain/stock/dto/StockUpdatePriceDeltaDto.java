package io.khw.domain.stock.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class StockUpdatePriceDeltaDto {

    private Long stockId;

    private String stockCode;

    private BigDecimal buyPrice;
}
