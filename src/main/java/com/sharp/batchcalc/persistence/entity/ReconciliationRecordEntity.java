package com.sharp.batchcalc.persistence.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "reconciliation_record")
public class ReconciliationRecordEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_header_id", nullable = false)
    @JsonIgnore
    private BatchHeader batchHeader;

    @Column(nullable = false)
    private String itemNumber;
    private String description;
    private String unitOfMeasure;

    private double qtyIssued;
    private double theoreticalRequired;
    private double qtyActuallyUsed;
    private double qtyRejected;
    private double qtyToReturn;
    private double variancePct;
    private boolean withinTolerance;

    @Column(nullable = false, updatable = false)
    private Instant computedAt = Instant.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public BatchHeader getBatchHeader() { return batchHeader; }
    public void setBatchHeader(BatchHeader batchHeader) { this.batchHeader = batchHeader; }
    public String getItemNumber() { return itemNumber; }
    public void setItemNumber(String itemNumber) { this.itemNumber = itemNumber; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getUnitOfMeasure() { return unitOfMeasure; }
    public void setUnitOfMeasure(String unitOfMeasure) { this.unitOfMeasure = unitOfMeasure; }
    public double getQtyIssued() { return qtyIssued; }
    public void setQtyIssued(double qtyIssued) { this.qtyIssued = qtyIssued; }
    public double getTheoreticalRequired() { return theoreticalRequired; }
    public void setTheoreticalRequired(double theoreticalRequired) { this.theoreticalRequired = theoreticalRequired; }
    public double getQtyActuallyUsed() { return qtyActuallyUsed; }
    public void setQtyActuallyUsed(double qtyActuallyUsed) { this.qtyActuallyUsed = qtyActuallyUsed; }
    public double getQtyRejected() { return qtyRejected; }
    public void setQtyRejected(double qtyRejected) { this.qtyRejected = qtyRejected; }
    public double getQtyToReturn() { return qtyToReturn; }
    public void setQtyToReturn(double qtyToReturn) { this.qtyToReturn = qtyToReturn; }
    public double getVariancePct() { return variancePct; }
    public void setVariancePct(double variancePct) { this.variancePct = variancePct; }
    public boolean isWithinTolerance() { return withinTolerance; }
    public void setWithinTolerance(boolean withinTolerance) { this.withinTolerance = withinTolerance; }
    public Instant getComputedAt() { return computedAt; }
    public void setComputedAt(Instant computedAt) { this.computedAt = computedAt; }
}
