package io.khw.trading.domaintest;

import io.khw.domain.stock.entity.StockEntity;
import io.khw.domain.transaction.entity.TransactionEntity;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransactionEntityTest {

    @Test
    @DisplayName("")
    public void createTransactionEntityFail(){
        StockEntity stock = StockEntity.builder().id(42L).code("377300").name("카카오페이").price(new BigDecimal(68400)).build();
        TransactionEntity transaction = TransactionEntity.builder().id(1L).stockId(20L).volume(5).price(new BigDecimal(65000.0)).transactionTime(LocalDateTime.now()).build();


        Assertions.assertEquals(1L, transaction.getId());
    }

    @Test
    @DisplayName("거래 수량 예외 테스트")
    public void testInvalidVolume() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new TransactionEntity( 1L, 0, BigDecimal.valueOf(100), LocalDateTime.now());
        });
    }

    @Test
    @DisplayName("거래 가격 예외 테스트")
    public void testInvalidPrice() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new TransactionEntity( 1L, 10, BigDecimal.ZERO, LocalDateTime.now());
        });
    }

    @Test
    @DisplayName("거래 시간 예외 테스트")
    public void testInvalidTransactionTime() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new TransactionEntity( 1L, 10, BigDecimal.valueOf(100), LocalDateTime.now().plusDays(1));
        });
    }
}
