package io.khw.domain.transaction.converter;

import io.khw.domain.transaction.dto.TransactionApiDto;
import io.khw.domain.transaction.dto.TransactionSaveDto;
import io.khw.domain.transaction.entity.TransactionEntity;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2023-06-11T15:43:52+0900",
    comments = "version: 1.5.3.Final, compiler: javac, environment: Java 17.0.4.1 (Eclipse Adoptium)"
)
@Component
public class TransactionConverterImpl implements TransactionConverter {

    @Override
    public TransactionEntity transactionSaveDtoToTransactionEntity(TransactionSaveDto transactionSaveDto) {
        if ( transactionSaveDto == null ) {
            return null;
        }

        TransactionEntity.TransactionEntityBuilder transactionEntity = TransactionEntity.builder();

        transactionEntity.stockId( transactionSaveDto.getStockId() );
        transactionEntity.volume( transactionSaveDto.getVolume() );
        transactionEntity.price( transactionSaveDto.getPrice() );
        transactionEntity.transactionTime( transactionSaveDto.getTransactionTime() );

        return transactionEntity.build();
    }

    @Override
    public TransactionApiDto toTransactionApiDto(TransactionEntity entity) {
        if ( entity == null ) {
            return null;
        }

        TransactionApiDto.TransactionApiDtoBuilder transactionApiDto = TransactionApiDto.builder();

        transactionApiDto.id( entity.getId() );
        transactionApiDto.stockId( entity.getStockId() );
        transactionApiDto.volume( entity.getVolume() );
        transactionApiDto.price( entity.getPrice() );
        transactionApiDto.transactionTime( entity.getTransactionTime() );

        return transactionApiDto.build();
    }
}
