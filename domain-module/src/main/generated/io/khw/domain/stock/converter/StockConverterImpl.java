package io.khw.domain.stock.converter;

import io.khw.domain.stock.dto.StockApiDto;
import io.khw.domain.stock.dto.StockPriceDeltaRankApiDto;
import io.khw.domain.stock.entity.StockEntity;
import java.time.LocalDateTime;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2023-06-14T16:38:15+0900",
    comments = "version: 1.5.3.Final, compiler: javac, environment: Java 17.0.6 (Eclipse Adoptium)"
)
@Component
public class StockConverterImpl implements StockConverter {

    @Override
    public StockApiDto toStockApiDto(StockEntity entity) {
        if ( entity == null ) {
            return null;
        }

        Long id = null;
        String code = null;
        String name = null;
        LocalDateTime createdAt = null;
        LocalDateTime updatedAt = null;

        id = entity.getId();
        code = entity.getCode();
        name = entity.getName();
        createdAt = entity.getCreatedAt();
        updatedAt = entity.getUpdatedAt();

        String price = null;

        StockApiDto stockApiDto = new StockApiDto( id, code, name, price, createdAt, updatedAt );

        formatPrice( stockApiDto, entity );

        return stockApiDto;
    }

    @Override
    public StockPriceDeltaRankApiDto toStockPriceDeltaRankApiDto(StockEntity entity) {
        if ( entity == null ) {
            return null;
        }

        Long id = null;
        String code = null;
        String name = null;
        LocalDateTime createdAt = null;
        LocalDateTime updatedAt = null;

        id = entity.getId();
        code = entity.getCode();
        name = entity.getName();
        createdAt = entity.getCreatedAt();
        updatedAt = entity.getUpdatedAt();

        String price = null;
        double priceDeltaPercentage = 0.0d;

        StockPriceDeltaRankApiDto stockPriceDeltaRankApiDto = new StockPriceDeltaRankApiDto( id, code, name, price, priceDeltaPercentage, createdAt, updatedAt );

        formatPrice( stockPriceDeltaRankApiDto, entity );

        return stockPriceDeltaRankApiDto;
    }
}
