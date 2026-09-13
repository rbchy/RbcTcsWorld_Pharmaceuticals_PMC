package com.sharp.batchcalc.persistence.repository;

import com.sharp.batchcalc.persistence.entity.ReconciliationRecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReconciliationRecordJpaRepository extends JpaRepository<ReconciliationRecordEntity, Long> {
    List<ReconciliationRecordEntity> findByBatchHeaderId(Long batchHeaderId);
}
