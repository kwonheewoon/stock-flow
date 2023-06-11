package io.khw.domain.stock.repository;

import io.khw.domain.stock.entity.StockEntity;
import io.khw.domain.transaction.entity.TransactionEntity;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StockRepository extends R2dbcRepository<StockEntity, Long> {


}
