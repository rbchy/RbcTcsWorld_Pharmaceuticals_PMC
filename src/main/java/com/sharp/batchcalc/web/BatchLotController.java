package com.sharp.batchcalc.web;

import com.sharp.batchcalc.*;
import com.sharp.batchcalc.web.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST API for the pharmaceutical packaging Batch/Lot Calculator.
 * ব্যাচ/লট ক্যালকুলেটরের জন্য REST API
 *
 * Base URL (default): http://localhost:8081/api/batch
 * STATELESS — nothing is saved. For persisted batches see BatchPersistenceController
 * at /api/batch/records.
 */
@RestController
@RequestMapping("/api/batch")
@Tag(name = "Stateless calculations", description = "Nothing is saved — each call computes and returns a result only.")
public class BatchLotController {

    private final BatchLotService service;

    public BatchLotController(BatchLotService service) {
        this.service = service;
    }

    @Operation(summary = "Start a batch", description = "Full pack-hierarchy breakdown (BFUs, caplets, bundles, shippers) for the ordered carton quantity.")
    @PostMapping("/hierarchy")
    public BatchHierarchy hierarchy(@Valid @RequestBody BatchRequest request) {
        return service.computeHierarchy(request);
    }

    @Operation(summary = "Material requirement", description = "Full-batch material issue quantities (base + overage) for each material in the request.")
    @PostMapping("/material-requirement")
    public List<BatchLotCalculator.MaterialLineResult> materialRequirement(@Valid @RequestBody BatchRequest request) {
        return service.computeMaterialRequirement(request);
    }

    @Operation(summary = "Partial production progress", description = "Given a target and a shift log, returns cumulative produced, remaining to finish, and % complete.")
    @PostMapping("/progress")
    public ProgressResult progress(@Valid @RequestBody ProgressRequest request) {
        return service.computeProgress(request);
    }

    @Operation(summary = "Material reconciliation", description = "Used / rejected / return-unused per material, with a variance-vs-tolerance flag.")
    @PostMapping("/reconciliation")
    public List<ReconciliationLine> reconciliation(@Valid @RequestBody ReconciliationRequest request) {
        return service.computeReconciliation(request);
    }

    @Operation(summary = "Sealed vs Exhaust lot allocation", description = "Draws down Open-Partial/Exhaust lots first (by priority), then Sealed lots only as needed. Returns HTTP 422 if the lots can't cover the requirement.")
    @PostMapping("/lot-allocation")
    public List<LotAllocationResult> lotAllocation(@Valid @RequestBody LotAllocationRequest request) {
        return service.allocateFromLots(request);
    }

    @Operation(summary = "Lot type classification", description = "Classifies each shift entry as Startup (first entry), Running, or Finished (target reached).")
    @PostMapping("/lot-type")
    public List<LotTypeResult> lotType(@Valid @RequestBody ProgressRequest request) {
        return service.classifyLotTypes(request);
    }
}
