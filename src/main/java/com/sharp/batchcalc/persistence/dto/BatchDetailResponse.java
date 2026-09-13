package com.sharp.batchcalc.persistence.dto;

import com.sharp.batchcalc.persistence.entity.*;

import java.time.Instant;
import java.util.List;

/** GET /api/batch/records/{id} response shape — a flat snapshot of everything saved for one batch. */
public class BatchDetailResponse {
    public Long id;
    public String workOrderNo;
    public String itemNumber;
    public String description;
    public String batchLotNo;
    public String customerName;
    public int capletsPerBfu, bfuPerCarton, cartonPerBundle, bundlePerShipper;
    public double overagePct;
    public double cartonsOrdered;
    public Instant createdAt;

    public List<MaterialRequirementEntity> materials;
    public List<ProductionEntryEntity> productionLog;
    public List<LotBalanceEntity> lotBalances;
    public List<ReconciliationRecordEntity> reconciliationRecords;

    public static BatchDetailResponse from(BatchHeader h) {
        BatchDetailResponse r = new BatchDetailResponse();
        r.id = h.getId();
        r.workOrderNo = h.getWorkOrderNo();
        r.itemNumber = h.getItemNumber();
        r.description = h.getDescription();
        r.batchLotNo = h.getBatchLotNo();
        r.customerName = h.getCustomerName();
        r.capletsPerBfu = h.getCapletsPerBfu();
        r.bfuPerCarton = h.getBfuPerCarton();
        r.cartonPerBundle = h.getCartonPerBundle();
        r.bundlePerShipper = h.getBundlePerShipper();
        r.overagePct = h.getOveragePct();
        r.cartonsOrdered = h.getCartonsOrdered();
        r.createdAt = h.getCreatedAt();
        r.materials = h.getMaterials();
        r.productionLog = h.getProductionLog();
        r.lotBalances = h.getLotBalances();
        r.reconciliationRecords = h.getReconciliationRecords();
        return r;
    }
}
