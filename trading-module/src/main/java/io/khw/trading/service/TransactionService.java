package io.khw.trading.service;

import io.khw.common.constants.ApiResponseCode;
import io.khw.domain.stock.repository.StockRepository;
import io.khw.domain.transaction.converter.TransactionConverter;
import io.khw.domain.transaction.dto.TransactionApiDto;
import io.khw.domain.transaction.dto.TransactionSaveDto;
import io.khw.domain.transaction.entity.TransactionEntity;
import io.khw.domain.transaction.repository.TransactionRepository;
import io.khw.common.exeception.TradingException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionConverter transactionConverter;

    private final TransactionRepository transactionRepository;

    private final StockRepository stockRepository;

    @Transactional
    public Mono<TransactionApiDto> saveTransaction(TransactionSaveDto transactionSaveDto) {

        TransactionEntity transactionEntity = transactionConverter.transactionSaveDtoToTransactionEntity(transactionSaveDto);

        return stockRepository.findById(transactionEntity.getStockId())
                .switchIfEmpty(Mono.error(new TradingException(ApiResponseCode.STOCK_NOT_FOUND)))
                .flatMap(stock -> transactionRepository.save(transactionEntity)
                        .flatMap(savedTransaction -> {
                            stock.updatePrice(savedTransaction.getPrice());
                            return stockRepository.save(stock).thenReturn(savedTransaction);
                        })
                ).onErrorResume(throwable -> Mono.error(new TradingException(ApiResponseCode.SERVER_ERROR)))
                .map(transactionConverter::toTransactionApiDto);
    }
}
