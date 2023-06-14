package io.khw.domain.stock.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class StockIncVolumeDto {

    private Long stockId;

    private String stockCode;

    private double tradeVolume;
}
