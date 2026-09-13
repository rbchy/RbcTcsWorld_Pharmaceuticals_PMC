package com.sharp.batchcalc.web;

import com.sharp.batchcalc.*;
import com.sharp.batchcalc.web.dto.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Maps web DTOs to the core (framework-agnostic) calculator classes and back.
 * The core com.sharp.batchcalc classes have no Spring/Jackson dependency —
 * this is the only place that bridges the two.
 * ওয়েব DTO আর মূল ক্যালকুলেটর ক্লাসের মধ্যে সেতু — শুধু এই ক্লাসেই Spring/Jackson নির্ভরতা
 */
@Service
public class BatchLotService {

    // ---------------------------------------------------------------
    // Mappers: DTO -> domain
    // ---------------------------------------------------------------
    private BatchConfig toConfig(BatchConfigRequest r) {
        return new BatchConfig(
                r.getWorkOrderNo(), r.getItemNumber(), r.getDescription(), r.getBatchLotNo(),
                r.getCustomerName(), r.getCapletsPerBfu(), r.getBfuPerCarton(),
                r.getCartonPerBundle(), r.getBundlePerShipper(), r.getOveragePct());
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

    private List<ProductionEntry> toLog(List<ProductionEntryRequest> list) {
        List<ProductionEntry> out = new ArrayList<>();
        for (ProductionEntryRequest e : list) {
            out.add(new ProductionEntry(e.getDate(), e.getShift(), e.getCartonsProduced(),
                    e.getCartonsRejected(), e.getRemarks()));
        }
        return out;
    }

    private List<LotBalance> toLots(List<LotBalanceRequest> list) {
        List<LotBalance> out = new ArrayList<>();
        for (LotBalanceRequest l : list) {
            out.add(new LotBalance(l.getLotId(), l.getStatus(), l.getPriority(),
                    l.getOpeningBalance(), l.getNominalQty(), l.getRemarks()));
        }
        return out;
    }

    // ---------------------------------------------------------------
    // 1) Hierarchy
    // ---------------------------------------------------------------
    public BatchHierarchy computeHierarchy(BatchRequest req) {
        BatchLotCalculator calc = new BatchLotCalculator(toConfig(req.getConfig()), List.of());
        return calc.computeHierarchy(req.getCartonsOrdered());
    }

    // ---------------------------------------------------------------
    // 2) Material requirement (full batch)
    // ---------------------------------------------------------------
    public List<BatchLotCalculator.MaterialLineResult> computeMaterialRequirement(BatchRequest req) {
        BatchLotCalculator calc = new BatchLotCalculator(toConfig(req.getConfig()), toMaterials(req.getMaterials()));
        return calc.computeMaterialRequirement(req.getCartonsOrdered());
    }

    // ---------------------------------------------------------------
    // 3) Progress (partial production)
    // ---------------------------------------------------------------
    public ProgressResult computeProgress(ProgressRequest req) {
        // Progress doesn't need config/materials — use a minimal dummy config.
        BatchLotCalculator calc = new BatchLotCalculator(dummyConfig(), List.of());
        return calc.computeProgress(req.getTargetCartons(), toLog(req.getLog()));
    }

    // ---------------------------------------------------------------
    // 4) Reconciliation
    // ---------------------------------------------------------------
    public List<ReconciliationLine> computeReconciliation(ReconciliationRequest req) {
        BatchLotCalculator calc = new BatchLotCalculator(toConfig(req.getConfig()), toMaterials(req.getMaterials()));
        double tolerance = req.getTolerance() != null ? req.getTolerance() : BatchLotCalculator.DEFAULT_TOLERANCE;
        Map<String, Double> issued = req.getQtyIssuedByItem() != null ? req.getQtyIssuedByItem() : new HashMap<>();
        Map<String, Double> used = req.getQtyUsedByItem() != null ? req.getQtyUsedByItem() : new HashMap<>();
        Map<String, Double> rejected = req.getQtyRejectedByItem() != null ? req.getQtyRejectedByItem() : new HashMap<>();
        return calc.computeReconciliation(req.getActualCartonsProduced(), issued, used, rejected, tolerance);
    }

    // ---------------------------------------------------------------
    // 5) Lot allocation — Sealed vs Exhaust
    // ---------------------------------------------------------------
    public List<LotAllocationResult> allocateFromLots(LotAllocationRequest req) {
        BatchLotCalculator calc = new BatchLotCalculator(dummyConfig(), List.of());
        return calc.allocateFromLots(req.getTotalRequirement(), toLots(req.getLots()));
    }

    // ---------------------------------------------------------------
    // 6) Lot type classification — Startup/Running/Finished
    // ---------------------------------------------------------------
    public List<LotTypeResult> classifyLotTypes(ProgressRequest req) {
        BatchLotCalculator calc = new BatchLotCalculator(dummyConfig(), List.of());
        return calc.classifyLotTypes(req.getTargetCartons(), toLog(req.getLog()));
    }

    /** Used only by endpoints that don't need pack-hierarchy ratios (progress/allocation/lot-type). */
    private BatchConfig dummyConfig() {
        return new BatchConfig("N/A", "N/A", "N/A", "N/A", "N/A", 1, 1, 1, 1, 0.0);
    }
}
