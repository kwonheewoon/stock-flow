package io.khw.stock.repository;

import io.khw.common.exeception.TradingException;
import io.khw.domain.stock.entity.StockEntity;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StockRepositoryTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private StockRepository stockRepository;

    @Test
    @DisplayName("주식 가격, 업데이트 시각 업데이트")
    void testUpdatePriceSuccessfulStock() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        StockEntity stockEntity = StockEntity.builder().id(42L).code("377300").name("카카오페이").price(new BigDecimal(68400)).createdAt(now.minusDays(1)).updatedAt(now.minusDays(1)).build();
        StockEntity updatedStockEntity = StockEntity.builder().id(42L).code("377300").name("카카오페이").price(new BigDecimal(69000)).createdAt(now.minusDays(1)).updatedAt(now).build();

        when(stockRepository.findById(anyLong())).thenReturn(Mono.just(stockEntity));
        when(stockRepository.save(any(StockEntity.class))).thenReturn(Mono.just(updatedStockEntity));


        // Act
        StockEntity findStockEntity = stockRepository.findById(42L).block();
        findStockEntity.updatePrice(new BigDecimal(69000));

        StockEntity result = stockRepository.save(findStockEntity).block();

        // Assert
        assertNotNull(result);
        assertEquals(updatedStockEntity.getId(), result.getId());
        assertEquals(updatedStockEntity.getCode(), result.getCode());
        assertEquals(updatedStockEntity.getName(), result.getName());
        assertEquals(updatedStockEntity.getPrice(), result.getPrice());
        assertEquals(updatedStockEntity.getUpdatedAt(), result.getUpdatedAt());
    }

    @Test
    @DisplayName("주식 가격 업데이트 실패(거래 가격 예외) 테스트")
    void testUpdatePriceFailed() {
        // Arrange
        assertThrows(TradingException.class, () -> {
            StockEntity updatedStockEntity = StockEntity.builder().id(42L).code("377300").name("카카오페이").price(new BigDecimal(69000)).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();
            updatedStockEntity.updatePrice(new BigDecimal(0));
            when(stockRepository.save(any(StockEntity.class))).thenReturn(Mono.just(updatedStockEntity));
            // Act
            stockRepository.save(updatedStockEntity).block();
        });
    }


}