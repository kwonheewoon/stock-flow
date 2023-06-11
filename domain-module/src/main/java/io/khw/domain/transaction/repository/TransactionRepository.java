package io.khw.domain.transaction.repository;

import io.khw.domain.transaction.entity.TransactionEntity;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

public interface TransactionRepository extends R2dbcRepository<TransactionEntity, Long> {

    Mono<TransactionEntity> findById(Long id);
}
