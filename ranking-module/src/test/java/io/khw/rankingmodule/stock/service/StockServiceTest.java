package io.khw.rankingmodule.stock.service;

import io.khw.domain.stock.converter.StockConverter;
import io.khw.domain.stock.dto.StockApiDto;
import io.khw.domain.stock.entity.StockEntity;
import io.khw.domain.stock.repository.StockRepository;
import io.khw.rankingmodule.stock.StockService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
public class StockServiceTest {

    @InjectMocks
    private StockService stockService;

    @Mock
    private StockRepository stockRepository;

    @Mock
    private StockConverter stockConverter;

    @Test
    @DisplayName("주식 상세보기")
    public void testfindStock(){
        // Arrange
        StockApiDto stockApiDto = new StockApiDto(42L, "377300","카카오페이", new BigDecimal(68400), LocalDateTime.now(), LocalDateTime.now());
        StockEntity findStockEntity = StockEntity.builder().id(42L).code("377300").name("카카오페이").price(new BigDecimal(68400)).build();

        when(stockRepository.findById(any(Long.class))).thenReturn(Mono.just(findStockEntity));
        when(stockConverter.toStockApiDto(any(StockEntity.class))).thenReturn(stockApiDto);

        // Act
        StockApiDto findStockApiDto = stockService.findStock(42L).block();

        // Assert
        assertNotNull(findStockApiDto);
        assertEquals(findStockApiDto.getId(), findStockEntity.getId());
        assertEquals(findStockApiDto.getName(), findStockEntity.getName());
        assertEquals(findStockApiDto.getPrice(), findStockEntity.getPrice());
    }
}
