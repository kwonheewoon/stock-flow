package io.khw.domain.transaction.converter;



import io.khw.domain.transaction.dto.TransactionApiDto;
import io.khw.domain.transaction.dto.TransactionSaveDto;
import io.khw.domain.transaction.entity.TransactionEntity;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TransactionConverter {

    TransactionEntity transactionSaveDtoToTransactionEntity(TransactionSaveDto transactionSaveDto);

    TransactionApiDto toTransactionApiDto(TransactionEntity entity);

}
