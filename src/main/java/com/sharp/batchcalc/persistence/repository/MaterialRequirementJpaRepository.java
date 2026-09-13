package com.sharp.batchcalc.persistence.repository;

import com.sharp.batchcalc.persistence.entity.MaterialRequirementEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MaterialRequirementJpaRepository extends JpaRepository<MaterialRequirementEntity, Long> {
    List<MaterialRequirementEntity> findByBatchHeaderId(Long batchHeaderId);
}
