package io.khw.trading.repositorytest;

import io.khw.domain.stock.repository.StockRepository;
import io.khw.domain.transaction.entity.TransactionEntity;
import io.khw.domain.transaction.repository.TransactionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionRepositoryTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private StockRepository stockRepository;

    @Test
    @DisplayName("트랜잭션 save 성공 테스트")
    void testSaveSuccessfulTransaction() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        TransactionEntity transaction = new TransactionEntity(20L, 10, BigDecimal.valueOf(1000), now);
        when(transactionRepository.save(any(TransactionEntity.class))).thenReturn(Mono.just(TransactionEntity.builder().id(1L).stockId(20L).volume(10).price(new BigDecimal(1000)).transactionTime(now).build()));


        // Act
        TransactionEntity result = transactionRepository.save(transaction).block();

        // Assert
        assertNotNull(result);
        assertEquals(transaction.getStockId(), result.getStockId());
        assertEquals(transaction.getVolume(), result.getVolume());
        assertEquals(transaction.getPrice(), result.getPrice());
        assertEquals(transaction.getTransactionTime(), result.getTransactionTime());
    }

    @Test
    @DisplayName("트랜잭션 save 실패(거래수량 예외) 테스트")
    void testSaveFailedTransactionWithZeroVolume() {
        // Arrange
        assertThrows(IllegalArgumentException.class, () -> {
            TransactionEntity transaction = new TransactionEntity(1L, 0, BigDecimal.valueOf(1000), LocalDateTime.now());
            when(transactionRepository.save(any(TransactionEntity.class))).thenReturn(Mono.just(transaction));
            // Act
            transactionRepository.save(transaction).block();
        });
    }

    @Test
    @DisplayName("트랜잭션 save 실패(거래 가격 예외) 테스트")
    void testSaveFailedTransactionWithZeroOrNegativePrice() {
        assertThrows(IllegalArgumentException.class, () -> {
            TransactionEntity transaction = new TransactionEntity(1L, 10, BigDecimal.ZERO, LocalDateTime.now());

            when(transactionRepository.save(any(TransactionEntity.class))).thenReturn(Mono.just(transaction));

            transactionRepository.save(transaction).block();
        });

        assertThrows(IllegalArgumentException.class, () -> {
            TransactionEntity transaction = new TransactionEntity(1L, 10, BigDecimal.valueOf(-1000), LocalDateTime.now());

            when(transactionRepository.save(any(TransactionEntity.class))).thenReturn(Mono.just(transaction));

            transactionRepository.save(transaction).block();
        });
    }

    @Test
    @DisplayName("트랜잭션 save 실패(현재 시간보다 미래에 대한 예외) 테스트")
    void testSaveFailedTransactionWithFutureTransactionTime() {
        assertThrows(IllegalArgumentException.class, () -> {
            TransactionEntity transaction = new TransactionEntity(1L, 10, BigDecimal.valueOf(1000), LocalDateTime.now().plusDays(1));
            when(transactionRepository.save(any(TransactionEntity.class))).thenReturn(Mono.just(transaction));

            transactionRepository.save(transaction).block();
        });
    }
}