package com.sharp.batchcalc.persistence.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sharp.batchcalc.LotStatus;
import jakarta.persistence.*;

@Entity
@Table(name = "lot_balance")
public class LotBalanceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_header_id", nullable = false)
    @JsonIgnore
    private BatchHeader batchHeader;

    /** Which material this lot belongs to (e.g. "106944" for foil), or a label like "BULK_CAPLET". */
    @Column(nullable = false)
    private String materialItemNumber;

    @Column(nullable = false)
    private String lotId;
    @Enumerated(EnumType.STRING)
    private LotStatus status;      // status BEFORE this allocation
    private int priority;
    private double openingBalance;
    private double nominalQty;
    private String remarks;

    // computed at allocation time
    private Double qtyIssued;
    private Double closingBalance;
    @Enumerated(EnumType.STRING)
    private LotStatus newStatus;   // status AFTER this allocation

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public BatchHeader getBatchHeader() { return batchHeader; }
    public void setBatchHeader(BatchHeader batchHeader) { this.batchHeader = batchHeader; }
    public String getMaterialItemNumber() { return materialItemNumber; }
    public void setMaterialItemNumber(String materialItemNumber) { this.materialItemNumber = materialItemNumber; }
    public String getLotId() { return lotId; }
    public void setLotId(String lotId) { this.lotId = lotId; }
    public LotStatus getStatus() { return status; }
    public void setStatus(LotStatus status) { this.status = status; }
    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }
    public double getOpeningBalance() { return openingBalance; }
    public void setOpeningBalance(double openingBalance) { this.openingBalance = openingBalance; }
    public double getNominalQty() { return nominalQty; }
    public void setNominalQty(double nominalQty) { this.nominalQty = nominalQty; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
    public Double getQtyIssued() { return qtyIssued; }
    public void setQtyIssued(Double qtyIssued) { this.qtyIssued = qtyIssued; }
    public Double getClosingBalance() { return closingBalance; }
    public void setClosingBalance(Double closingBalance) { this.closingBalance = closingBalance; }
    public LotStatus getNewStatus() { return newStatus; }
    public void setNewStatus(LotStatus newStatus) { this.newStatus = newStatus; }
}
