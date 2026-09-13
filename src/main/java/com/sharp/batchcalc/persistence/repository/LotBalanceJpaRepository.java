package com.sharp.batchcalc.persistence.repository;

import com.sharp.batchcalc.persistence.entity.LotBalanceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LotBalanceJpaRepository extends JpaRepository<LotBalanceEntity, Long> {
    List<LotBalanceEntity> findByBatchHeaderId(Long batchHeaderId);
    List<LotBalanceEntity> findByBatchHeaderIdAndMaterialItemNumber(Long batchHeaderId, String materialItemNumber);
}
