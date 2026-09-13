package com.sharp.batchcalc.persistence.repository;

import com.sharp.batchcalc.persistence.entity.BatchHeader;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BatchHeaderRepository extends JpaRepository<BatchHeader, Long> {
    List<BatchHeader> findByWorkOrderNo(String workOrderNo);
}
