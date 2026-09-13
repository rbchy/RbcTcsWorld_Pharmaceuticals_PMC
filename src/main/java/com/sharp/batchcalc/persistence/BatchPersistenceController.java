package com.sharp.batchcalc.persistence;

import com.sharp.batchcalc.persistence.dto.*;
import com.sharp.batchcalc.persistence.entity.*;
import com.sharp.batchcalc.web.dto.BatchRequest;
import com.sharp.batchcalc.web.dto.ProductionEntryRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Persisted batch records — a NEW, independent schema for this calculator
 * (batch_header, material_requirement, production_entry, lot_balance,
 * reconciliation_record). Stateless calculations still work unchanged at
 * /api/batch/* (see BatchLotController) — these endpoints additionally save
 * the results so batch history can be looked up later.
 *
 * সংরক্ষিত ব্যাচ রেকর্ড — এই ক্যালকুলেটরের জন্য নতুন, স্বাধীন স্কিমা
 */
@RestController
@RequestMapping("/api/batch/records")
@Tag(name = "Persisted batch records", description = "Saves everything to MySQL (a new, independent schema — not the pharma-packaging-system's 9-table schema).")
public class BatchPersistenceController {

    private final BatchPersistenceService service;

    public BatchPersistenceController(BatchPersistenceService service) {
        this.service = service;
    }

    @Operation(summary = "Create a batch", description = "Saves the header + materials (base/overage/total computed and stored).")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BatchDetailResponse createBatch(@Valid @RequestBody BatchRequest request) {
        BatchHeader saved = service.createBatch(request);
        return service.getBatchDetail(saved.getId());
    }

    @Operation(summary = "List batches", description = "Summary only (id, work order, batch/lot no, cartons ordered, created date) — for full detail use GET /{id}.")
    @GetMapping
    public List<BatchSummaryResponse> listBatches() {
        return service.listBatches();
    }

    @Operation(summary = "Get full batch detail", description = "Materials, production log, lot balances, and reconciliation records for one batch.")
    @GetMapping("/{id}")
    public BatchDetailResponse getBatch(@PathVariable Long id) {
        return service.getBatchDetail(id);
    }

    @Operation(summary = "Append production entries",
        description = "APPEND-ONLY, not idempotent — each call adds whatever you send as brand-new rows. "
            + "Only send genuinely new shift entries; resending old ones creates duplicates.")
    @PostMapping("/{id}/production")
    public List<ProductionEntryEntity> appendProduction(@PathVariable Long id,
                                                         @Valid @RequestBody List<ProductionEntryRequest> entries) {
        return service.appendProduction(id, entries);
    }

    @Operation(summary = "Delete a batch", description = "Cascades to materials, production log, lot balances, and reconciliation records.")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBatch(@PathVariable Long id) {
        service.deleteBatch(id);
    }

    @Operation(summary = "Delete one production entry", description = "For removing an accidental duplicate. Note: cumulative/outstanding values already saved on OTHER entries do not auto-recompute.")
    @DeleteMapping("/{id}/production/{entryId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProductionEntry(@PathVariable Long id, @PathVariable Long entryId) {
        service.deleteProductionEntry(id, entryId);
    }

    @Operation(summary = "Save Sealed vs Exhaust lot allocation", description = "Runs the allocation and persists one row per lot, tagged with materialItemNumber.")
    @PostMapping("/{id}/lot-allocation")
    public List<LotBalanceEntity> saveLotAllocation(@PathVariable Long id,
                                                     @Valid @RequestBody PersistLotAllocationRequest request) {
        return service.saveLotAllocation(id, request);
    }

    @Operation(summary = "Save reconciliation", description = "Uses the batch's own saved materials/config — you only send actual production/usage figures.")
    @PostMapping("/{id}/reconciliation")
    public List<ReconciliationRecordEntity> saveReconciliation(@PathVariable Long id,
                                                                @Valid @RequestBody PersistReconciliationRequest request) {
        return service.saveReconciliation(id, request);
    }
}
