package com.sharp.batchcalc.persistence.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "material_requirement")
public class MaterialRequirementEntity {

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
    private double baseQtyPer1000Cartons;

    // computed at save time, for convenience when reading back
    private Double baseRequired;
    private Double overageQty;
    private Double totalToIssue;

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
    public double getBaseQtyPer1000Cartons() { return baseQtyPer1000Cartons; }
    public void setBaseQtyPer1000Cartons(double baseQtyPer1000Cartons) { this.baseQtyPer1000Cartons = baseQtyPer1000Cartons; }
    public Double getBaseRequired() { return baseRequired; }
    public void setBaseRequired(Double baseRequired) { this.baseRequired = baseRequired; }
    public Double getOverageQty() { return overageQty; }
    public void setOverageQty(Double overageQty) { this.overageQty = overageQty; }
    public Double getTotalToIssue() { return totalToIssue; }
    public void setTotalToIssue(Double totalToIssue) { this.totalToIssue = totalToIssue; }
}
