package io.khw.stock.repository;

import io.khw.TradingModuleApplication;
import io.khw.domain.stock.entity.StockEntity;
import io.khw.domain.stock.repository.StockRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
@SpringBootTest(classes = TradingModuleApplication.class,webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class StockRepositoryIntTest {


    @Autowired
    private StockRepository stockRepository;

    @BeforeEach
    public void setup() {
        // 테스트 데이터를 세팅
        StockEntity stock = StockEntity.builder().code("999999").name("카카오페이 증권").price(new BigDecimal(68400)).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();

        stockRepository.save(stock).block();
    }

    @Test
    public void testFindById() {
        StockEntity stock = stockRepository.findById(121L).block();


        Assertions.assertNotNull(stock);
        Assertions.assertEquals(121L, stock.getId());
        Assertions.assertEquals("999999", stock.getCode());
        Assertions.assertEquals("카카오페이 증권", stock.getName());
    }
}
