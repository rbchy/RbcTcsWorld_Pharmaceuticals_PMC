package com.sharp.batchcalc.persistence.repository;

import com.sharp.batchcalc.persistence.entity.ProductionEntryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductionEntryJpaRepository extends JpaRepository<ProductionEntryEntity, Long> {
    List<ProductionEntryEntity> findByBatchHeaderIdOrderByEntryDateAscIdAsc(Long batchHeaderId);
}
