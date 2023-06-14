package io.khw.ranking.stock;

import io.khw.domain.stock.converter.StockConverter;
import io.khw.domain.stock.dto.StockApiDto;
import io.khw.domain.stock.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class StockService {

    private final StockRepository stockRepository;

    private final StockConverter stockConverter;

    public Mono<StockApiDto> findStock(Long id){
        return stockRepository.findById(id).map(stockConverter::toStockApiDto);
    }
}
