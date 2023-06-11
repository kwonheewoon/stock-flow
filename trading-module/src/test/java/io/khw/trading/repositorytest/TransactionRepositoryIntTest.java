package io.khw.trading.repositorytest;

import io.khw.TradingModuleApplication;
import io.khw.domain.stock.entity.StockEntity;
import io.khw.domain.stock.repository.StockRepository;
import io.khw.domain.transaction.entity.TransactionEntity;
import io.khw.domain.transaction.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


import java.math.BigDecimal;
import java.time.LocalDateTime;
@SpringBootTest(classes = TradingModuleApplication.class,webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TransactionRepositoryIntTest {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private StockRepository stockRepository;

    @BeforeEach
    public void setup() {
        // 테스트 데이터를 세팅
        StockEntity stock = StockEntity.builder().code("377300").name("카카오페이").price(new BigDecimal(68400)).build();
        TransactionEntity transaction = TransactionEntity.builder().stockId(42L).volume(5).price(new BigDecimal(65000.0)).transactionTime(LocalDateTime.now()).build();


        //stockRepository.save(stock).block();


        transactionRepository.save(transaction).block();
    }

    @Test
    public void testFindById() throws InterruptedException {
        TransactionEntity transaction = transactionRepository.findById(1L).block();
        Assertions.assertNotNull(transaction);
        Assertions.assertEquals(1L, transaction.getId());

        StockEntity stock = stockRepository.findById(transaction.getStockId()).block();
        Assertions.assertNotNull(stock);
        Assertions.assertEquals("377300", stock.getCode());

        Thread.sleep(500L);
    }
}
