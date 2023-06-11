package io.khw.trading.service;

import io.khw.domain.stock.entity.StockEntity;
import io.khw.domain.stock.repository.StockRepository;
import io.khw.domain.transaction.converter.TransactionConverter;
import io.khw.domain.transaction.dto.TransactionApiDto;
import io.khw.domain.transaction.dto.TransactionSaveDto;
import io.khw.domain.transaction.entity.TransactionEntity;
import io.khw.domain.transaction.repository.TransactionRepository;
import io.khw.common.exeception.TradingException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
public class TransactionServiceTest {

    @InjectMocks
    private TransactionService transactionService;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private StockRepository stockRepository;

    @Mock
    private TransactionConverter transactionConverter;

    @Test
    @DisplayName("트랜잭션 저장 및 주식 가격 업데이트 성공 테스트")
    public void testSaveTransactionAndUpdateStockPrice() {
        // Arrange
        TransactionSaveDto transactionSaveDto = new TransactionSaveDto(42L, 10, BigDecimal.valueOf(1000), LocalDateTime.now());
        TransactionEntity transactionEntity = new TransactionEntity(42L, 10, BigDecimal.valueOf(1000), LocalDateTime.now());
        StockEntity stockEntity = StockEntity.builder().id(42L).code("377300").name("카카오페이").price(new BigDecimal(68400)).build();

        when(stockRepository.findById(any(Long.class))).thenReturn(Mono.just(stockEntity));
        when(transactionRepository.save(any(TransactionEntity.class))).thenReturn(Mono.just(transactionEntity));
        when(stockRepository.save(any(StockEntity.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        when(transactionConverter.transactionSaveDtoToTransactionEntity(transactionSaveDto)).thenReturn(transactionEntity);
        when(transactionConverter.toTransactionApiDto(transactionEntity)).thenReturn(new TransactionApiDto(1L,42L, 10, BigDecimal.valueOf(1000), LocalDateTime.now()));

        // Act
        TransactionApiDto resultTransaction = transactionService.saveTransaction(transactionSaveDto).block();

        // Assert
        assertNotNull(resultTransaction);
        assertEquals(transactionEntity.getStockId(), resultTransaction.getStockId());
        assertEquals(transactionEntity.getVolume(), resultTransaction.getVolume());
        assertEquals(transactionEntity.getPrice(), resultTransaction.getPrice());

        // Verify that the stockRepository's save method was called with the updated price
        ArgumentCaptor<StockEntity> captor = ArgumentCaptor.forClass(StockEntity.class);
        verify(stockRepository).save(captor.capture());
        assertEquals(transactionEntity.getPrice(), captor.getValue().getPrice());
    }

    @Test
    @DisplayName("주식이 존재하지 않을 때 트랜잭션 저장 실패 테스트")
    public void testSaveTransactionFailWhenStockNotFound() {
        // Arrange
        TransactionSaveDto transactionSaveDto = new TransactionSaveDto(42L, 10, BigDecimal.valueOf(1000), LocalDateTime.now());
        TransactionEntity transactionEntity = new TransactionEntity(42L, 10, BigDecimal.valueOf(1000), LocalDateTime.now());

        when(transactionConverter.transactionSaveDtoToTransactionEntity(transactionSaveDto)).thenReturn(transactionEntity);
        when(stockRepository.findById(any(Long.class))).thenReturn(Mono.empty());

        // Act & Assert
        assertThrows(TradingException.class, () -> transactionService.saveTransaction(transactionSaveDto).block());
        verify(stockRepository).findById(any(Long.class));
        //stockRepository의 findById 외에 상호작용이 없음을 검증
        verifyNoMoreInteractions(stockRepository);
        //transactionRepository와 상호작용이 없음을 검증
        verifyNoInteractions(transactionRepository);
    }

    @Test
    @DisplayName("트랜잭션 저장 실패 테스트")
    public void testSaveTransactionFailWhenSaveFails() {
        // Arrange
        TransactionSaveDto transactionSaveDto = new TransactionSaveDto(42L, 10, BigDecimal.valueOf(1000), LocalDateTime.now());
        TransactionEntity transactionEntity = new TransactionEntity(42L, 10, BigDecimal.valueOf(1000), LocalDateTime.now());
        StockEntity stockEntity = StockEntity.builder().id(42L).code("377300").name("카카오페이").price(new BigDecimal(68400)).build();

        when(transactionConverter.transactionSaveDtoToTransactionEntity(transactionSaveDto)).thenReturn(transactionEntity);
        when(stockRepository.findById(any(Long.class))).thenReturn(Mono.just(stockEntity));
        when(transactionRepository.save(any(TransactionEntity.class))).thenThrow(new RuntimeException());

        // Act & Assert
        assertThrows(TradingException.class, () -> transactionService.saveTransaction(transactionSaveDto).block());
        verify(stockRepository).findById(any(Long.class));
        verify(transactionRepository).save(any(TransactionEntity.class));
    }


}
