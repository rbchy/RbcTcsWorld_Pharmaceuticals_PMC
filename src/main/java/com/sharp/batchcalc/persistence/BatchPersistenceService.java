package com.sharp.batchcalc.persistence;

import com.sharp.batchcalc.*;
import com.sharp.batchcalc.persistence.dto.*;
import com.sharp.batchcalc.persistence.entity.*;
import com.sharp.batchcalc.persistence.repository.*;
import com.sharp.batchcalc.web.dto.*;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Persists BatchLotCalculator results into the new, independent schema
 * (batch_header, material_requirement, production_entry, lot_balance,
 * reconciliation_record). Core calculator classes stay untouched — this
 * service is the only place that knows about JPA.
 * ক্যালকুলেটরের ফলাফল ডেটাবেসে সংরক্ষণ করে — মূল ক্যালকুলেটর ক্লাস অপরিবর্তিত থাকে
 */
@Service
public class BatchPersistenceService {

    private final BatchHeaderRepository batchHeaderRepo;
    private final MaterialRequirementJpaRepository materialRepo;
    private final ProductionEntryJpaRepository productionRepo;
    private final LotBalanceJpaRepository lotBalanceRepo;
    private final ReconciliationRecordJpaRepository reconciliationRepo;

    public BatchPersistenceService(BatchHeaderRepository batchHeaderRepo,
                                    MaterialRequirementJpaRepository materialRepo,
                                    ProductionEntryJpaRepository productionRepo,
                                    LotBalanceJpaRepository lotBalanceRepo,
                                    ReconciliationRecordJpaRepository reconciliationRepo) {
        this.batchHeaderRepo = batchHeaderRepo;
        this.materialRepo = materialRepo;
        this.productionRepo = productionRepo;
        this.lotBalanceRepo = lotBalanceRepo;
        this.reconciliationRepo = reconciliationRepo;
    }

    // ---------------------------------------------------------------
    // Create a batch (header + materials), computing base/overage/total
    // ---------------------------------------------------------------
    @Transactional
    public BatchHeader createBatch(BatchRequest req) {
        BatchConfigRequest c = req.getConfig();

        BatchHeader header = new BatchHeader();
        header.setWorkOrderNo(c.getWorkOrderNo());
        header.setItemNumber(c.getItemNumber());
        header.setDescription(c.getDescription());
        header.setBatchLotNo(c.getBatchLotNo());
        header.setCustomerName(c.getCustomerName());
        header.setCapletsPerBfu(c.getCapletsPerBfu());
        header.setBfuPerCarton(c.getBfuPerCarton());
        header.setCartonPerBundle(c.getCartonPerBundle());
        header.setBundlePerShipper(c.getBundlePerShipper());
        header.setOveragePct(c.getOveragePct());
        header.setCartonsOrdered(req.getCartonsOrdered());

        BatchConfig config = toConfig(c);
        List<MaterialRequirement> materials = toMaterials(req.getMaterials());
        BatchLotCalculator calc = new BatchLotCalculator(config, materials);
        List<BatchLotCalculator.MaterialLineResult> computed = calc.computeMaterialRequirement(req.getCartonsOrdered());

        for (BatchLotCalculator.MaterialLineResult m : computed) {
            MaterialRequirementEntity e = new MaterialRequirementEntity();
            e.setBatchHeader(header);
            e.setItemNumber(m.material.getItemNumber());
            e.setDescription(m.material.getDescription());
            e.setUnitOfMeasure(m.material.getUnitOfMeasure());
            e.setBaseQtyPer1000Cartons(m.material.getBaseQtyPer1000Cartons());
            e.setBaseRequired(m.baseRequired);
            e.setOverageQty(m.overageQty);
            e.setTotalToIssue(m.totalToIssue);
            header.getMaterials().add(e);
        }

        return batchHeaderRepo.save(header);
    }

    // ---------------------------------------------------------------
    // Append production entries, recomputing lot type / cumulative / outstanding
    // over the FULL log (existing + new) so numbers stay consistent
    // ---------------------------------------------------------------
    @Transactional
    public List<ProductionEntryEntity> appendProduction(Long batchId, List<ProductionEntryRequest> newEntries) {
        BatchHeader header = getBatchOrThrow(batchId);

        List<ProductionEntryEntity> existing =
                productionRepo.findByBatchHeaderIdOrderByEntryDateAscIdAsc(batchId);

        // Convert existing + new into core ProductionEntry objects, in order
        List<ProductionEntry> fullLog = new ArrayList<>();
        for (ProductionEntryEntity e : existing) {
            fullLog.add(new ProductionEntry(e.getEntryDate(), e.getShift(), e.getCartonsProduced(),
                    e.getCartonsRejected(), e.getRemarks()));
        }
        for (ProductionEntryRequest r : newEntries) {
            fullLog.add(new ProductionEntry(r.getDate(), r.getShift(), r.getCartonsProduced(),
                    r.getCartonsRejected(), r.getRemarks()));
        }

        BatchLotCalculator calc = new BatchLotCalculator(toConfig(header), List.of());
        List<LotTypeResult> classified = calc.classifyLotTypes(header.getCartonsOrdered(), fullLog);

        // Persist only the newly added entries (existing ones keep their saved computed values
        // from when they were appended — target doesn't change, so re-deriving is unnecessary
        // unless you want to also re-stamp history; kept simple here).
        List<ProductionEntryEntity> savedNew = new ArrayList<>();
        int startIdx = existing.size();
        for (int i = 0; i < newEntries.size(); i++) {
            ProductionEntryRequest r = newEntries.get(i);
            LotTypeResult classification = startIdx + i < classified.size() ? classified.get(startIdx + i) : null;

            ProductionEntryEntity e = new ProductionEntryEntity();
            e.setBatchHeader(header);
            e.setEntryDate(r.getDate());
            e.setShift(r.getShift());
            e.setCartonsProduced(r.getCartonsProduced());
            e.setCartonsRejected(r.getCartonsRejected());
            e.setRemarks(r.getRemarks());
            if (classification != null) {
                e.setLotType(classification.lotType);
                e.setCumulativeProduced(classification.cumulativeProduced);
                e.setOutstanding(classification.outstanding);
            }
            ProductionEntryEntity persisted = productionRepo.save(e);
            header.getProductionLog().add(persisted); // keep in-memory association in sync (see note below)
            savedNew.add(persisted);
        }
        return savedNew;
    }

    // ---------------------------------------------------------------
    // Lot allocation (Sealed vs Exhaust), persisted per material
    // ---------------------------------------------------------------
    @Transactional
    public List<LotBalanceEntity> saveLotAllocation(Long batchId, PersistLotAllocationRequest req) {
        BatchHeader header = getBatchOrThrow(batchId);

        List<LotBalance> lots = new ArrayList<>();
        for (LotBalanceRequest l : req.getLots()) {
            lots.add(new LotBalance(l.getLotId(), l.getStatus(), l.getPriority(),
                    l.getOpeningBalance(), l.getNominalQty(), l.getRemarks()));
        }

        BatchLotCalculator calc = new BatchLotCalculator(toConfig(header), List.of());
        List<LotAllocationResult> results = calc.allocateFromLots(req.getTotalRequirement(), lots);

        List<LotBalanceEntity> saved = new ArrayList<>();
        for (int i = 0; i < req.getLots().size(); i++) {
            LotBalanceRequest src = req.getLots().get(i);
            // allocateFromLots sorts by priority internally; match back by lotId
            LotAllocationResult result = results.stream()
                    .filter(r -> r.lotId.equals(src.getLotId()))
                    .findFirst().orElseThrow();

            LotBalanceEntity e = new LotBalanceEntity();
            e.setBatchHeader(header);
            e.setMaterialItemNumber(req.getMaterialItemNumber());
            e.setLotId(src.getLotId());
            e.setStatus(src.getStatus());
            e.setPriority(src.getPriority());
            e.setOpeningBalance(src.getOpeningBalance());
            e.setNominalQty(src.getNominalQty());
            e.setRemarks(src.getRemarks());
            e.setQtyIssued(result.qtyIssued);
            e.setClosingBalance(result.closingBalance);
            e.setNewStatus(result.newStatus);
            LotBalanceEntity persisted = lotBalanceRepo.save(e);
            header.getLotBalances().add(persisted);
            saved.add(persisted);
        }
        return saved;
    }

    // ---------------------------------------------------------------
    // Reconciliation, using the batch's OWN saved materials + config
    // ---------------------------------------------------------------
    @Transactional
    public List<ReconciliationRecordEntity> saveReconciliation(Long batchId, PersistReconciliationRequest req) {
        BatchHeader header = getBatchOrThrow(batchId);

        List<MaterialRequirementEntity> savedMaterials = materialRepo.findByBatchHeaderId(batchId);
        List<MaterialRequirement> materials = new ArrayList<>();
        for (MaterialRequirementEntity m : savedMaterials) {
            materials.add(new MaterialRequirement(m.getItemNumber(), m.getDescription(),
                    m.getUnitOfMeasure(), m.getBaseQtyPer1000Cartons()));
        }

        BatchLotCalculator calc = new BatchLotCalculator(toConfig(header), materials);
        double tolerance = req.getTolerance() != null ? req.getTolerance() : BatchLotCalculator.DEFAULT_TOLERANCE;
        Map<String, Double> issued = req.getQtyIssuedByItem() != null ? req.getQtyIssuedByItem() : new HashMap<>();
        Map<String, Double> used = req.getQtyUsedByItem() != null ? req.getQtyUsedByItem() : new HashMap<>();
        Map<String, Double> rejected = req.getQtyRejectedByItem() != null ? req.getQtyRejectedByItem() : new HashMap<>();

        List<ReconciliationLine> lines = calc.computeReconciliation(
                req.getActualCartonsProduced(), issued, used, rejected, tolerance);

        List<ReconciliationRecordEntity> saved = new ArrayList<>();
        for (ReconciliationLine line : lines) {
            ReconciliationRecordEntity e = new ReconciliationRecordEntity();
            e.setBatchHeader(header);
            e.setItemNumber(line.itemNumber);
            e.setDescription(line.description);
            e.setUnitOfMeasure(line.unitOfMeasure);
            e.setQtyIssued(line.qtyIssued);
            e.setTheoreticalRequired(line.theoreticalRequired);
            e.setQtyActuallyUsed(line.qtyActuallyUsed);
            e.setQtyRejected(line.qtyRejected);
            e.setQtyToReturn(line.qtyToReturn);
            e.setVariancePct(line.variancePct);
            e.setWithinTolerance(line.withinTolerance);
            ReconciliationRecordEntity persisted = reconciliationRepo.save(e);
            header.getReconciliationRecords().add(persisted);
            saved.add(persisted);
        }
        return saved;
    }

    // ---------------------------------------------------------------
    // Reads
    // ---------------------------------------------------------------
    @Transactional(readOnly = true)
    public BatchDetailResponse getBatchDetail(Long batchId) {
        BatchHeader header = getBatchOrThrow(batchId);
        // Force-initialize the lazy collections HERE, inside the open transaction,
        // rather than relying on Spring Boot's open-in-view default (which keeps
        // the Hibernate session open through response serialization but is best
        // not to depend on implicitly).
        org.hibernate.Hibernate.initialize(header.getMaterials());
        org.hibernate.Hibernate.initialize(header.getProductionLog());
        org.hibernate.Hibernate.initialize(header.getLotBalances());
        org.hibernate.Hibernate.initialize(header.getReconciliationRecords());
        return BatchDetailResponse.from(header);
    }

    @Transactional(readOnly = true)
    public List<BatchSummaryResponse> listBatches() {
        List<BatchSummaryResponse> out = new ArrayList<>();
        for (BatchHeader h : batchHeaderRepo.findAll()) {
            out.add(BatchSummaryResponse.from(h));
        }
        return out;
    }

    /** Deletes a batch and everything under it (materials, production log, lot balances, reconciliation) — cascades via orphanRemoval. */
    @Transactional
    public void deleteBatch(Long batchId) {
        BatchHeader header = getBatchOrThrow(batchId);
        batchHeaderRepo.delete(header);
    }

    /** Deletes one production_entry row (e.g. to remove an accidental duplicate). */
    @Transactional
    public void deleteProductionEntry(Long batchId, Long entryId) {
        BatchHeader header = getBatchOrThrow(batchId); // 404 if batch itself doesn't exist
        productionRepo.deleteById(entryId);
        // Keep in-memory association in sync too, in case this batch is read again
        // within the same transaction (see note on appendProduction/saveLotAllocation/
        // saveReconciliation above).
        header.getProductionLog().removeIf(e -> entryId.equals(e.getId()));
    }

    // ---------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------
    private BatchHeader getBatchOrThrow(Long id) {
        return batchHeaderRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Batch not found: id=" + id));
    }

    private BatchConfig toConfig(BatchConfigRequest r) {
        return new BatchConfig(r.getWorkOrderNo(), r.getItemNumber(), r.getDescription(), r.getBatchLotNo(),
                r.getCustomerName(), r.getCapletsPerBfu(), r.getBfuPerCarton(),
                r.getCartonPerBundle(), r.getBundlePerShipper(), r.getOveragePct());
    }

    private BatchConfig toConfig(BatchHeader h) {
        return new BatchConfig(h.getWorkOrderNo(), h.getItemNumber(), h.getDescription(), h.getBatchLotNo(),
                h.getCustomerName(), h.getCapletsPerBfu(), h.getBfuPerCarton(),
                h.getCartonPerBundle(), h.getBundlePerShipper(), h.getOveragePct());
    }

    private List<MaterialRequirement> toMaterials(List<MaterialRequirementRequest> list) {
        List<MaterialRequirement> out = new ArrayList<>();
        if (list == null) return out;
        for (MaterialRequirementRequest m : list) {
            out.add(new MaterialRequirement(m.getItemNumber(), m.getDescription(),
                    m.getUnitOfMeasure(), m.getBaseQtyPer1000Cartons()));
        }
        return out;
    }
}
