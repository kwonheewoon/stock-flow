package io.khw.domain.transaction.converter;

import io.khw.domain.transaction.dto.TransactionApiDto;
import io.khw.domain.transaction.dto.TransactionSaveDto;
import io.khw.domain.transaction.entity.TransactionEntity;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2023-06-16T00:05:10+0900",
    comments = "version: 1.5.3.Final, compiler: javac, environment: Java 17.0.6 (Eclipse Adoptium)"
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

        Long id = null;
        Long stockId = null;
        int volume = 0;
        BigDecimal price = null;
        LocalDateTime transactionTime = null;

        id = entity.getId();
        stockId = entity.getStockId();
        volume = entity.getVolume();
        price = entity.getPrice();
        transactionTime = entity.getTransactionTime();

        TransactionApiDto transactionApiDto = new TransactionApiDto( id, stockId, volume, price, transactionTime );

        return transactionApiDto;
    }
}
