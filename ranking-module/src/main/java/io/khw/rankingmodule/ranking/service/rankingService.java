package io.khw.rankingmodule.ranking.service;

import io.khw.domain.stock.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class rankingService {

    private final StockRepository stockRepository;



}
