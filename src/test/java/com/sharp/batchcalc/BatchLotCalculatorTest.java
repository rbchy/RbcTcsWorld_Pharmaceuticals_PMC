package com.sharp.batchcalc;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * JUnit 5 regression tests for BatchLotCalculator.
 * BatchLotCalculator-এর জন্য JUnit 5 রিগ্রেশন টেস্ট।
 *
 * IMPORTANT: this file must live under src/test/java/com/sharp/batchcalc/
 * (not src/main/java) — Maven's default build only scans src/test/java for
 * tests, which is why "No tests to run" appeared when this class sat in
 * src/main/java with no @Test annotations.
 *
 * pom.xml needs the JUnit Jupiter dependency (test scope):
 *
 *   <dependency>
 *     <groupId>org.junit.jupiter</groupId>
 *     <artifactId>junit-jupiter</artifactId>
 *     <version>5.10.2</version>
 *     <scope>test</scope>
 *   </dependency>
 *
 * maven-surefire-plugin 3.x (already in your build log: surefire:3.2.5)
 * auto-detects JUnit 5 once that dependency is present — no extra provider
 * needed. Run with:  mvn test
 */
@Epic("Unit Tests — Core Calculator (BatchLotCalculator)")
public class BatchLotCalculatorTest {

    // ---------------------------------------------------------------
    // Fixtures — same data as the uploaded work order S180406-1
    // ---------------------------------------------------------------
    private static BatchConfig sampleConfig() {
        return new BatchConfig("S180406-1", "503892", "Imodium MSR 24ct caplets Non CR 302126200",
                "FFC103", "Kenvue Brands LLC (McNeil)", 6, 4, 6, 6, 1.0 / 24.0);
    }

    private static List<MaterialRequirement> sampleMaterials() {
        return Arrays.asList(
                new MaterialRequirement("106944", "Vinyl Klockner PA 160/02 .010 PVC 220mm (foil)", "lb", 27.97),
                new MaterialRequirement("125112", "Shipper - Imodium Generic 30048350", "EA", 27.78),
                new MaterialRequirement("129913", "Carton - Imodium MSR 24ct Cap NonCR 30054636", "EA", 1000.00)
        );
    }

    private static final double CARTONS_ORDERED = 62916;

    // ---------------------------------------------------------------
    // 1) Hierarchy
    // ---------------------------------------------------------------
    @Feature("Hierarchy")
    @Severity(SeverityLevel.CRITICAL)
    @Test
    void hierarchyMatchesWorkOrder() {
        BatchLotCalculator calc = new BatchLotCalculator(sampleConfig(), sampleMaterials());
        BatchHierarchy h = calc.computeHierarchy(CARTONS_ORDERED);

        assertEquals(251664.0, h.bfus, 0.01, "BFUs = Cartons x BFUs/Carton (matches AQL report Lot Size A)");
        assertEquals(1509984.0, h.caplets, 0.01, "Caplets = BFUs x Caplets/BFU");
        assertEquals(10486.0, h.bundles, 0.5, "Bundles = Cartons / Cartons-per-Bundle (matches AQL Lot Size D)");
        assertEquals(1748L, h.shippers, "Shippers rounded up (matches AQL Lot Size C = 1,748)");
    }

    // ---------------------------------------------------------------
    // 2) Material requirement
    // ---------------------------------------------------------------
    @Feature("Material Requirement")
    @Severity(SeverityLevel.CRITICAL)
    @Test
    void materialRequirementMatchesWorkOrder() {
        BatchLotCalculator calc = new BatchLotCalculator(sampleConfig(), sampleMaterials());
        List<BatchLotCalculator.MaterialLineResult> results = calc.computeMaterialRequirement(CARTONS_ORDERED);

        double cartonTotal = results.stream()
                .filter(r -> r.material.getItemNumber().equals("129913"))
                .findFirst().get().totalToIssue;
        // Work order printed "Required Qty to Issue" for item 129913 = 65,538.0 EA
        assertEquals(65538.0, cartonTotal, 1.0, "Carton total-to-issue matches printed work order");
    }

    // ---------------------------------------------------------------
    // 3) Progress — partial and complete
    // ---------------------------------------------------------------
    @Feature("Progress")
    @Severity(SeverityLevel.CRITICAL)
    @Test
    void progressPartialProduction() {
        BatchLotCalculator calc = new BatchLotCalculator(sampleConfig(), sampleMaterials());
        List<ProductionEntry> log = Arrays.asList(
                new ProductionEntry(LocalDate.of(2026, 7, 9), "A", 8500, 50, ""),
                new ProductionEntry(LocalDate.of(2026, 7, 9), "B", 8200, 30, ""),
                new ProductionEntry(LocalDate.of(2026, 7, 10), "A", 9000, 0, "")
        );
        ProgressResult p = calc.computeProgress(CARTONS_ORDERED, log);

        assertEquals(25700.0, p.cumulativeProducedCartons, 0.01);
        assertEquals(37216.0, p.remainingCartons, 0.01);
        assertFalse(p.complete);
    }

    @Feature("Progress")
    @Severity(SeverityLevel.CRITICAL)
    @Test
    void progressBatchComplete() {
        BatchLotCalculator calc = new BatchLotCalculator(sampleConfig(), sampleMaterials());
        List<ProductionEntry> log = Arrays.asList(
                new ProductionEntry(LocalDate.of(2026, 7, 9), "A", CARTONS_ORDERED, 0, "")
        );
        ProgressResult p = calc.computeProgress(CARTONS_ORDERED, log);

        assertEquals(0.0, p.remainingCartons, 0.01);
        assertTrue(p.complete);
    }

    // ---------------------------------------------------------------
    // 4) Reconciliation — tolerance
    // ---------------------------------------------------------------
    @Feature("Reconciliation")
    @Severity(SeverityLevel.CRITICAL)
    @Test
    void reconciliationWithinTolerance() {
        BatchLotCalculator calc = new BatchLotCalculator(sampleConfig(), sampleMaterials());
        Map<String, Double> issued = new HashMap<>();
        issued.put("106944", 1833.08);
        Map<String, Double> used = new HashMap<>();
        used.put("106944", 756.27);
        Map<String, Double> rejected = new HashMap<>();
        rejected.put("106944", 35.0);

        List<ReconciliationLine> lines = calc.computeReconciliation(
                25700, issued, used, rejected, BatchLotCalculator.DEFAULT_TOLERANCE);
        ReconciliationLine foil = lines.get(0);

        assertTrue(foil.withinTolerance, "1% variance is within +/-3% default tolerance");
        assertTrue(calc.allWithinTolerance(lines));
    }

    @Feature("Reconciliation")
    @Severity(SeverityLevel.CRITICAL)
    @Test
    void reconciliationOutOfTolerance() {
        BatchLotCalculator calc = new BatchLotCalculator(sampleConfig(), sampleMaterials());
        Map<String, Double> issued = new HashMap<>();
        issued.put("106944", 1833.08);
        Map<String, Double> used = new HashMap<>();
        used.put("106944", 900.0); // ~20% above theoretical (~748.8)
        Map<String, Double> rejected = new HashMap<>();
        rejected.put("106944", 0.0);

        List<ReconciliationLine> lines = calc.computeReconciliation(
                25700, issued, used, rejected, BatchLotCalculator.DEFAULT_TOLERANCE);
        ReconciliationLine foil = lines.get(0);

        assertFalse(foil.withinTolerance, "Large variance should be flagged out-of-tolerance");
        assertFalse(calc.allWithinTolerance(lines));
    }

    // ---------------------------------------------------------------
    // 5) Lot allocation — Sealed vs Exhaust
    // ---------------------------------------------------------------
    @Feature("Lot Allocation")
    @Severity(SeverityLevel.CRITICAL)
    @Test
    void allocateFromLotsCascades() {
        BatchLotCalculator calc = new BatchLotCalculator(sampleConfig(), sampleMaterials());
        List<LotBalance> lots = Arrays.asList(
                new LotBalance("6CV1111", LotStatus.OPEN_PARTIAL, 1, 620000, 620000, "Exhaust"),
                new LotBalance("6CV1191", LotStatus.OPEN_PARTIAL, 2, 580000, 580000, "Exhaust"),
                new LotBalance("6CV1201", LotStatus.OPEN_PARTIAL, 3, 250000, 250000, "Exhaust"),
                new LotBalance("6CV1250", LotStatus.SEALED, 4, 500000, 500000, "Sealed top-up")
        );
        List<LotAllocationResult> results = calc.allocateFromLots(1509984, lots);

        assertEquals(4, results.size());
        assertEquals(LotStatus.EXHAUSTED, results.get(0).newStatus);
        assertEquals(LotStatus.EXHAUSTED, results.get(1).newStatus);
        assertEquals(LotStatus.EXHAUSTED, results.get(2).newStatus);
        assertEquals(59984.0, results.get(3).qtyIssued, 0.01, "Sealed lot supplies exactly the shortfall");
        assertEquals(LotStatus.OPEN_PARTIAL, results.get(3).newStatus, "Sealed lot becomes Open-Partial, carries forward");

        double totalIssued = results.stream().mapToDouble(r -> r.qtyIssued).sum();
        assertEquals(1509984.0, totalIssued, 0.01, "Total issued across all lots equals requirement exactly");
    }

    @Feature("Lot Allocation")
    @Severity(SeverityLevel.CRITICAL)
    @Test
    void allocateFromLotsShortfallThrows() {
        BatchLotCalculator calc = new BatchLotCalculator(sampleConfig(), sampleMaterials());
        List<LotBalance> lots = Arrays.asList(
                new LotBalance("LOT-A", LotStatus.OPEN_PARTIAL, 1, 100, 100, "too small")
        );
        assertThrows(IllegalStateException.class, () -> calc.allocateFromLots(1000, lots));
    }

    // ---------------------------------------------------------------
    // 6) Lot type classification — three-shift lifecycle
    // ---------------------------------------------------------------
    @Feature("Lot Type Classification")
    @Severity(SeverityLevel.CRITICAL)
    @Test
    void classifyLotTypesStartupRunningFinished() {
        BatchLotCalculator calc = new BatchLotCalculator(sampleConfig(), sampleMaterials());
        List<ProductionEntry> log = Arrays.asList(
                new ProductionEntry(LocalDate.of(2026, 7, 9), "A", 20000, 0, ""),
                new ProductionEntry(LocalDate.of(2026, 7, 9), "B", 20000, 0, ""),
                new ProductionEntry(LocalDate.of(2026, 7, 10), "A", 22916, 0, "")
        );
        List<LotTypeResult> results = calc.classifyLotTypes(CARTONS_ORDERED, log);

        assertEquals(3, results.size());
        assertEquals(LotType.STARTUP, results.get(0).lotType);
        assertEquals(LotType.RUNNING, results.get(1).lotType);
        assertEquals(LotType.FINISHED, results.get(2).lotType);
        assertEquals(0.0, results.get(2).outstanding, 0.01);
    }

    @Feature("Lot Type Classification")
    @Severity(SeverityLevel.NORMAL)
    @Test
    void classifyLotTypesSingleShiftFinish() {
        BatchLotCalculator calc = new BatchLotCalculator(sampleConfig(), sampleMaterials());
        List<ProductionEntry> log = Arrays.asList(
                new ProductionEntry(LocalDate.of(2026, 7, 9), "A", CARTONS_ORDERED, 0, "")
        );
        List<LotTypeResult> results = calc.classifyLotTypes(CARTONS_ORDERED, log);

        assertEquals(1, results.size());
        assertEquals(LotType.FINISHED, results.get(0).lotType,
                "A batch finished on its very first shift is Finished, not Startup");
    }

    // =================================================================
    // ADDITIONAL BOUNDARY / NEGATIVE TESTS
    // (maps to RbcTcsWorld_Test_Cases.xlsx, sheet "1-Unit Tests (Core)")
    // =================================================================

    // ---------------------------------------------------------------
    // TC-UNIT-002 / 003 / 004 — hierarchy boundary & negative inputs
    // ---------------------------------------------------------------
    @Feature("Hierarchy")
    @Severity(SeverityLevel.NORMAL)
    @Test
    void tc_unit_002_hierarchyZeroCartons() {
        BatchLotCalculator calc = new BatchLotCalculator(sampleConfig(), sampleMaterials());
        BatchHierarchy h = calc.computeHierarchy(0);

        assertEquals(0.0, h.bfus, 0.0001);
        assertEquals(0.0, h.caplets, 0.0001);
        assertEquals(0.0, h.bundles, 0.0001);
        assertEquals(0L, h.shippers, "Zero cartons produces zero shippers, no exception");
    }

    @Feature("Hierarchy")
    @Severity(SeverityLevel.MINOR)
    @Test
    void tc_unit_003_hierarchyNegativeCartons_documentsCurrentBehavior() {
        // No input validation exists today — negative cartons flow straight through
        // the arithmetic rather than being rejected. This test documents that
        // current (arguably undesirable) behavior so a future fix is a deliberate,
        // visible change to this test rather than a silent one.
        BatchLotCalculator calc = new BatchLotCalculator(sampleConfig(), sampleMaterials());
        BatchHierarchy h = calc.computeHierarchy(-100);

        assertEquals(-400.0, h.bfus, 0.0001);
        assertEquals(-2400.0, h.caplets, 0.0001);
        assertEquals(-2L, h.shippers, "No exception thrown for negative input — flagged as a hardening candidate");
    }

    @Feature("Hierarchy")
    @Severity(SeverityLevel.NORMAL)
    @Test
    void tc_unit_004_hierarchyShippersAlwaysWholeNumber() {
        BatchLotCalculator calc = new BatchLotCalculator(sampleConfig(), sampleMaterials());
        // 100 cartons / 6 per bundle = 16.67 bundles -> 16.67/6 = 2.78 -> ceil = 3
        BatchHierarchy h = calc.computeHierarchy(100);

        assertEquals(16.666666666666668, h.bundles, 0.0001);
        assertEquals(3L, h.shippers, "Shippers is always rounded UP to a whole number, never fractional");
    }

    // ---------------------------------------------------------------
    // TC-UNIT-006 / 007 — material requirement edge cases
    // ---------------------------------------------------------------
    @Feature("Material Requirement")
    @Severity(SeverityLevel.NORMAL)
    @Test
    void tc_unit_006_materialRequirementEmptyList() {
        BatchLotCalculator calc = new BatchLotCalculator(sampleConfig(), List.of());
        List<BatchLotCalculator.MaterialLineResult> results = calc.computeMaterialRequirement(CARTONS_ORDERED);

        assertTrue(results.isEmpty(), "Empty materials list returns empty results, no exception");
    }

    @Feature("Material Requirement")
    @Severity(SeverityLevel.MINOR)
    @Test
    void tc_unit_007_materialRequirementNegativeOveragePct_documentsCurrentBehavior() {
        BatchConfig negativeOverageConfig = new BatchConfig("WO", "ITEM", "desc", "LOT", "CUST",
                6, 4, 6, 6, -0.05); // -5% "overage" — no validation prevents this today
        List<MaterialRequirement> materials = List.of(
                new MaterialRequirement("X1", "Test material", "EA", 100.0));
        BatchLotCalculator calc = new BatchLotCalculator(negativeOverageConfig, materials);

        BatchLotCalculator.MaterialLineResult r = calc.computeMaterialRequirement(1000).get(0);

        assertEquals(100.0, r.baseRequired, 0.001);
        assertEquals(-5.0, r.overageQty, 0.001, "Negative overagePct produces negative overageQty");
        assertEquals(95.0, r.totalToIssue, 0.001, "totalToIssue ends up LOWER than base — flagged as a hardening candidate");
    }

    // ---------------------------------------------------------------
    // TC-UNIT-010 / 011 / 012 — progress boundary & negative inputs
    // ---------------------------------------------------------------
    @Feature("Progress")
    @Severity(SeverityLevel.NORMAL)
    @Test
    void tc_unit_010_progressOverproduction_cappedNotNegativeOrOver100Percent() {
        BatchLotCalculator calc = new BatchLotCalculator(sampleConfig(), sampleMaterials());
        List<ProductionEntry> log = List.of(
                new ProductionEntry(LocalDate.of(2026, 7, 9), "A", 1200, 0, "")
        );
        ProgressResult p = calc.computeProgress(1000, log);

        assertEquals(0.0, p.remainingCartons, 0.0001, "Remaining never goes negative when overproduced");
        assertEquals(1.0, p.percentComplete, 0.0001, "percentComplete is capped at 1.0 (100%), not 1.2");
        assertTrue(p.complete);
    }

    @Feature("Progress")
    @Severity(SeverityLevel.NORMAL)
    @Test
    void tc_unit_011_progressEmptyLog() {
        BatchLotCalculator calc = new BatchLotCalculator(sampleConfig(), sampleMaterials());
        ProgressResult p = calc.computeProgress(1000, List.of());

        assertEquals(0.0, p.cumulativeProducedCartons, 0.0001);
        assertEquals(1000.0, p.remainingCartons, 0.0001);
        assertFalse(p.complete);
    }

    @Feature("Progress")
    @Severity(SeverityLevel.NORMAL)
    @Test
    void tc_unit_012_progressZeroTarget_noDivideByZero() {
        BatchLotCalculator calc = new BatchLotCalculator(sampleConfig(), sampleMaterials());
        List<ProductionEntry> log = List.of(
                new ProductionEntry(LocalDate.of(2026, 7, 9), "A", 500, 0, "")
        );
        ProgressResult p = calc.computeProgress(0, log);

        assertEquals(0.0, p.percentComplete, 0.0001, "Zero target is special-cased to avoid dividing by zero");
        assertTrue(p.complete, "remaining <= 0 when target is 0, so batch is considered complete");
    }

    // ---------------------------------------------------------------
    // TC-UNIT-015 / 016 — lot type classification edge cases
    // ---------------------------------------------------------------
    @Feature("Lot Type Classification")
    @Severity(SeverityLevel.NORMAL)
    @Test
    void tc_unit_015_classifyLotTypesStopsAtFirstFinished() {
        BatchLotCalculator calc = new BatchLotCalculator(sampleConfig(), sampleMaterials());
        List<ProductionEntry> log = List.of(
                new ProductionEntry(LocalDate.of(2026, 7, 9), "A", 40, 0, ""),  // STARTUP, outstanding=60
                new ProductionEntry(LocalDate.of(2026, 7, 9), "B", 60, 0, ""),  // FINISHED, outstanding=0
                new ProductionEntry(LocalDate.of(2026, 7, 10), "A", 30, 0, ""), // never classified
                new ProductionEntry(LocalDate.of(2026, 7, 10), "B", 10, 0, "")  // never classified
        );
        List<LotTypeResult> results = calc.classifyLotTypes(100, log);

        assertEquals(2, results.size(), "Classification stops as soon as FINISHED is reached — later entries are not returned");
        assertEquals(LotType.STARTUP, results.get(0).lotType);
        assertEquals(LotType.FINISHED, results.get(1).lotType);
    }

    @Feature("Lot Type Classification")
    @Severity(SeverityLevel.MINOR)
    @Test
    void tc_unit_016_classifyLotTypesEmptyLog() {
        BatchLotCalculator calc = new BatchLotCalculator(sampleConfig(), sampleMaterials());
        List<LotTypeResult> results = calc.classifyLotTypes(1000, List.of());

        assertTrue(results.isEmpty(), "Empty log returns empty classification list, no exception");
    }

    // ---------------------------------------------------------------
    // TC-UNIT-019 / 020 / 021 — lot allocation boundary & negative
    // ---------------------------------------------------------------
    @Feature("Lot Allocation")
    @Severity(SeverityLevel.NORMAL)
    @Test
    void tc_unit_019_allocateFromLotsExactMatch_noShortfallException() {
        BatchLotCalculator calc = new BatchLotCalculator(sampleConfig(), sampleMaterials());
        List<LotBalance> lots = List.of(
                new LotBalance("LOT-1", LotStatus.OPEN_PARTIAL, 1, 600, 600, ""),
                new LotBalance("LOT-2", LotStatus.OPEN_PARTIAL, 2, 400, 400, "")
        );
        List<LotAllocationResult> results = calc.allocateFromLots(1000, lots);

        assertEquals(LotStatus.EXHAUSTED, results.get(0).newStatus);
        assertEquals(LotStatus.EXHAUSTED, results.get(1).newStatus,
                "Combined balance exactly equal to requirement fully exhausts both lots with no exception");
        assertEquals(0.0, results.get(1).closingBalance, 0.0001);
    }

    @Feature("Lot Allocation")
    @Severity(SeverityLevel.NORMAL)
    @Test
    void tc_unit_020_allocateFromLotsTiedPriority_stableOrderPreserved() {
        BatchLotCalculator calc = new BatchLotCalculator(sampleConfig(), sampleMaterials());
        // Both lots share priority=1 — Java's List.sort is a stable sort, so ties
        // preserve the original input order (LOT-FIRST before LOT-SECOND).
        List<LotBalance> lots = List.of(
                new LotBalance("LOT-FIRST", LotStatus.OPEN_PARTIAL, 1, 100, 100, ""),
                new LotBalance("LOT-SECOND", LotStatus.OPEN_PARTIAL, 1, 100, 100, "")
        );
        List<LotAllocationResult> results = calc.allocateFromLots(150, lots);

        assertEquals("LOT-FIRST", results.get(0).lotId, "Stable sort keeps input order for equal priorities");
        assertEquals(100.0, results.get(0).qtyIssued, 0.0001, "First lot drained fully before second is touched");
        assertEquals(50.0, results.get(1).qtyIssued, 0.0001);
    }

    @Feature("Lot Allocation")
    @Severity(SeverityLevel.NORMAL)
    @Test
    void tc_unit_021_allocateFromLotsEmptyList_throwsShortfall() {
        BatchLotCalculator calc = new BatchLotCalculator(sampleConfig(), sampleMaterials());
        assertThrows(IllegalStateException.class, () -> calc.allocateFromLots(100, List.of()),
                "Zero lots available can never satisfy a positive requirement");
    }

    // ---------------------------------------------------------------
    // TC-UNIT-024 / 025 / 026 — reconciliation boundary & negative
    // ---------------------------------------------------------------
    @Feature("Reconciliation")
    @Severity(SeverityLevel.NORMAL)
    @Test
    void tc_unit_024_reconciliationExactlyAtToleranceBoundary_isInclusive() {
        List<MaterialRequirement> materials = List.of(
                new MaterialRequirement("X1", "Test material", "EA", 1000.0)); // theoretical = 1000 for 1000 cartons, 0% overage config below
        BatchConfig zeroOverage = new BatchConfig("WO", "ITEM", "desc", "LOT", "CUST", 6, 4, 6, 6, 0.0);
        BatchLotCalculator calc = new BatchLotCalculator(zeroOverage, materials);

        Map<String, Double> used = new HashMap<>();
        used.put("X1", 1030.0); // exactly +3.0% over theoretical (1000)
        ReconciliationLine line = calc.computeReconciliation(1000, Map.of(), used, Map.of(), 0.03).get(0);

        assertEquals(0.03, line.variancePct, 0.0001);
        assertTrue(line.withinTolerance, "Variance exactly AT the tolerance boundary is inclusive (<=), not exclusive");
    }

    @Feature("Reconciliation")
    @Severity(SeverityLevel.MINOR)
    @Test
    void tc_unit_025_reconciliationMissingUsageEntry_defaultsToTheoretical_documentsCurrentBehavior() {
        // When an item has no entry in qtyUsedByItem, the calculator defaults "used"
        // to the theoretical requirement itself — which makes variance silently
        // read as 0% (perfect) rather than surfacing "no data was entered." This
        // test documents that current behavior as a known UX/data-integrity gap.
        BatchLotCalculator calc = new BatchLotCalculator(sampleConfig(), sampleMaterials());
        ReconciliationLine line = calc.computeReconciliation(
                25700, Map.of(), Map.of(), Map.of(), BatchLotCalculator.DEFAULT_TOLERANCE).get(0);

        assertEquals(0.0, line.variancePct, 0.0001,
                "Missing usage data reads as 0% variance instead of flagging missing input — hardening candidate");
        assertTrue(line.withinTolerance);
    }

    @Feature("Reconciliation")
    @Severity(SeverityLevel.MINOR)
    @Test
    void tc_unit_026_reconciliationNegativeRejected_flowsThroughArithmetic() {
        List<MaterialRequirement> materials = List.of(
                new MaterialRequirement("X1", "Test material", "EA", 100.0));
        BatchConfig zeroOverage = new BatchConfig("WO", "ITEM", "desc", "LOT", "CUST", 6, 4, 6, 6, 0.0);
        BatchLotCalculator calc = new BatchLotCalculator(zeroOverage, materials);

        Map<String, Double> issued = Map.of("X1", 100.0);
        Map<String, Double> used = Map.of("X1", 50.0);
        Map<String, Double> rejected = Map.of("X1", -10.0); // negative — no validation prevents this
        ReconciliationLine line = calc.computeReconciliation(1000, issued, used, rejected, 0.03).get(0);

        assertEquals(60.0, line.qtyToReturn, 0.0001,
                "qtyToReturn = issued - used - rejected; a negative reject INCREASES the return figure (100-50-(-10)=60)");
    }

    // ---------------------------------------------------------------
    // TC-UNIT-027 — null config
    // ---------------------------------------------------------------
    @Feature("Constructor / Input Validation")
    @Severity(SeverityLevel.MINOR)
    @Test
    void tc_unit_027_nullConfig_failsLateNotAtConstruction() {
        // The constructor performs no null-check, so construction itself succeeds;
        // the failure surfaces only on first use (fail-late, not fail-fast).
        BatchLotCalculator calc = new BatchLotCalculator(null, sampleMaterials());
        assertThrows(NullPointerException.class, () -> calc.computeHierarchy(1000),
                "Null config throws NPE on first method call, not at construction — consider a fail-fast constructor check");
    }
}
