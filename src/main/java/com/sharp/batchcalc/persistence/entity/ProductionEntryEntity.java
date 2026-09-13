package com.sharp.batchcalc.persistence.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sharp.batchcalc.LotType;
import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "production_entry")
public class ProductionEntryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_header_id", nullable = false)
    @JsonIgnore
    private BatchHeader batchHeader;

    @Column(nullable = false)
    private LocalDate entryDate;
    @Column(nullable = false)
    private String shift;
    private double cartonsProduced;
    private double cartonsRejected;
    private String remarks;

    // computed at save time (Startup/Running/Finished) and cumulative snapshot
    @Enumerated(EnumType.STRING)
    private LotType lotType;
    private Double cumulativeProduced;
    private Double outstanding;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public BatchHeader getBatchHeader() { return batchHeader; }
    public void setBatchHeader(BatchHeader batchHeader) { this.batchHeader = batchHeader; }
    public LocalDate getEntryDate() { return entryDate; }
    public void setEntryDate(LocalDate entryDate) { this.entryDate = entryDate; }
    public String getShift() { return shift; }
    public void setShift(String shift) { this.shift = shift; }
    public double getCartonsProduced() { return cartonsProduced; }
    public void setCartonsProduced(double cartonsProduced) { this.cartonsProduced = cartonsProduced; }
    public double getCartonsRejected() { return cartonsRejected; }
    public void setCartonsRejected(double cartonsRejected) { this.cartonsRejected = cartonsRejected; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
    public LotType getLotType() { return lotType; }
    public void setLotType(LotType lotType) { this.lotType = lotType; }
    public Double getCumulativeProduced() { return cumulativeProduced; }
    public void setCumulativeProduced(Double cumulativeProduced) { this.cumulativeProduced = cumulativeProduced; }
    public Double getOutstanding() { return outstanding; }
    public void setOutstanding(Double outstanding) { this.outstanding = outstanding; }
}
