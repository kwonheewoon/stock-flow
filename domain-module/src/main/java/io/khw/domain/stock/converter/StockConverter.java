package io.khw.domain.stock.converter;


import io.khw.domain.stock.dto.StockApiDto;
import io.khw.domain.stock.entity.StockEntity;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface StockConverter {

    @Mapping(target = "price", source = "price", ignore = true)
    StockApiDto toStockApiDto(StockEntity entity);

    @AfterMapping
    default void formatPrice(@MappingTarget StockApiDto stockApiDto, StockEntity stockEntity) {
        stockApiDto.formatPrice(stockEntity.getPrice());
    }

}
