package com.sharp.batchcalc.persistence.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Root record for one batch/work order — the pack-hierarchy config plus
 * everything computed for it (materials, production log, lot balances,
 * reconciliation). This is a NEW, independent schema for this calculator
 * module only (not tied to the pharma-packaging-system's 9-table schema).
 *
 * এই ক্যালকুলেটরের জন্য একটা নতুন, স্বাধীন স্কিমা — ব্যাচের মূল রেকর্ড
 */
@Entity
@Table(name = "batch_header")
public class BatchHeader {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String workOrderNo;
    @Column(nullable = false)
    private String itemNumber;
    private String description;
    private String batchLotNo;
    private String customerName;

    // Pack-hierarchy configuration
    private int capletsPerBfu;
    private int bfuPerCarton;
    private int cartonPerBundle;
    private int bundlePerShipper;
    private double overagePct;

    @Column(nullable = false)
    private double cartonsOrdered;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @OneToMany(mappedBy = "batchHeader", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<MaterialRequirementEntity> materials = new ArrayList<>();

    @OneToMany(mappedBy = "batchHeader", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<ProductionEntryEntity> productionLog = new ArrayList<>();

    @OneToMany(mappedBy = "batchHeader", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<LotBalanceEntity> lotBalances = new ArrayList<>();

    @OneToMany(mappedBy = "batchHeader", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<ReconciliationRecordEntity> reconciliationRecords = new ArrayList<>();

    // getters/setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getWorkOrderNo() { return workOrderNo; }
    public void setWorkOrderNo(String workOrderNo) { this.workOrderNo = workOrderNo; }
    public String getItemNumber() { return itemNumber; }
    public void setItemNumber(String itemNumber) { this.itemNumber = itemNumber; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getBatchLotNo() { return batchLotNo; }
    public void setBatchLotNo(String batchLotNo) { this.batchLotNo = batchLotNo; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public int getCapletsPerBfu() { return capletsPerBfu; }
    public void setCapletsPerBfu(int capletsPerBfu) { this.capletsPerBfu = capletsPerBfu; }
    public int getBfuPerCarton() { return bfuPerCarton; }
    public void setBfuPerCarton(int bfuPerCarton) { this.bfuPerCarton = bfuPerCarton; }
    public int getCartonPerBundle() { return cartonPerBundle; }
    public void setCartonPerBundle(int cartonPerBundle) { this.cartonPerBundle = cartonPerBundle; }
    public int getBundlePerShipper() { return bundlePerShipper; }
    public void setBundlePerShipper(int bundlePerShipper) { this.bundlePerShipper = bundlePerShipper; }
    public double getOveragePct() { return overagePct; }
    public void setOveragePct(double overagePct) { this.overagePct = overagePct; }
    public double getCartonsOrdered() { return cartonsOrdered; }
    public void setCartonsOrdered(double cartonsOrdered) { this.cartonsOrdered = cartonsOrdered; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public List<MaterialRequirementEntity> getMaterials() { return materials; }
    public void setMaterials(List<MaterialRequirementEntity> materials) { this.materials = materials; }
    public List<ProductionEntryEntity> getProductionLog() { return productionLog; }
    public void setProductionLog(List<ProductionEntryEntity> productionLog) { this.productionLog = productionLog; }
    public List<LotBalanceEntity> getLotBalances() { return lotBalances; }
    public void setLotBalances(List<LotBalanceEntity> lotBalances) { this.lotBalances = lotBalances; }
    public List<ReconciliationRecordEntity> getReconciliationRecords() { return reconciliationRecords; }
    public void setReconciliationRecords(List<ReconciliationRecordEntity> reconciliationRecords) { this.reconciliationRecords = reconciliationRecords; }
}
