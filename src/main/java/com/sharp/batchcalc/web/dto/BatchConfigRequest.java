package com.sharp.batchcalc.web.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * JSON request body matching BatchConfig — kept as a separate mutable DTO
 * (rather than annotating the core BatchConfig class) so the calculator
 * module stays framework-agnostic and reusable outside Spring.
 * JSON রিকোয়েস্ট বডি — মূল ক্যালকুলেটর ক্লাসগুলো Spring-নির্ভর না রাখতে আলাদা DTO
 */
public class BatchConfigRequest {

    @NotBlank
    private String workOrderNo;
    @NotBlank
    private String itemNumber;
    private String description;
    private String batchLotNo;
    private String customerName;

    @Min(1)
    private int capletsPerBfu;
    @Min(1)
    private int bfuPerCarton;
    @Min(1)
    private int cartonPerBundle;
    @Min(1)
    private int bundlePerShipper;

    @NotNull
    private Double overagePct;

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
    public Double getOveragePct() { return overagePct; }
    public void setOveragePct(Double overagePct) { this.overagePct = overagePct; }
}
