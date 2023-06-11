package io.khw.domain.stock.converter;


import io.khw.domain.stock.dto.StockApiDto;
import io.khw.domain.stock.entity.StockEntity;
import io.khw.domain.transaction.dto.TransactionApiDto;
import io.khw.domain.transaction.dto.TransactionSaveDto;
import io.khw.domain.transaction.entity.TransactionEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface StockConverter {

    StockApiDto toStockApiDto(StockEntity entity);

}
