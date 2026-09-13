package com.sharp.batchcalc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Demo run using the sample work order (Sharp Packaging Solutions, S180406-1,
 * Kenvue Brands LLC / Imodium MSR 24ct caplets) exactly as uploaded.
 */
public class Main {

    public static void main(String[] args) {

        BatchConfig config = new BatchConfig(
                "S180406-1", "503892", "Imodium MSR 24ct caplets Non CR 302126200",
                "FFC103", "Kenvue Brands LLC (McNeil)",
                6, 4, 6, 6, 1.0 / 24.0
        );

        List<MaterialRequirement> materials = Arrays.asList(
                new MaterialRequirement("106944", "Vinyl Klockner PA 160/02 .010 PVC 220mm (foil)", "lb", 27.97),
                new MaterialRequirement("125112", "Shipper - Imodium Generic 30048350", "EA", 27.78),
                new MaterialRequirement("129913", "Carton - Imodium MSR 24ct Cap NonCR 30054636", "EA", 1000.00)
        );

        BatchLotCalculator calc = new BatchLotCalculator(config, materials);
        double cartonsOrdered = 62916;

        System.out.println("========== 1) START BATCH - FULL HIERARCHY ==========");
        BatchHierarchy hierarchy = calc.computeHierarchy(cartonsOrdered);
        System.out.println(hierarchy);

        System.out.println("\n---- Material requirement to issue (full batch) ----");
        for (BatchLotCalculator.MaterialLineResult m : calc.computeMaterialRequirement(cartonsOrdered)) {
            System.out.println(m);
        }

        System.out.println("\n========== 2) PARTIAL PRODUCTION PROGRESS ==========");
        List<ProductionEntry> log = Arrays.asList(
                new ProductionEntry(LocalDate.of(2026, 7, 9), "A", 8500, 50, "Batch start / line clearance done"),
                new ProductionEntry(LocalDate.of(2026, 7, 9), "B", 8200, 30, ""),
                new ProductionEntry(LocalDate.of(2026, 7, 10), "A", 9000, 0, "")
        );
        ProgressResult progress = calc.computeProgress(cartonsOrdered, log);
        System.out.println(progress);

        System.out.println("\n---- Material still needed to FINISH the batch ----");
        for (BatchLotCalculator.MaterialLineResult m : calc.computeMaterialToFinish(progress)) {
            System.out.println(m);
        }

        System.out.println("\n========== 3) MATERIAL RECONCILIATION ==========");
        Map<String, Double> issued = new HashMap<>();
        for (BatchLotCalculator.MaterialLineResult m : calc.computeMaterialRequirement(cartonsOrdered)) {
            issued.put(m.material.getItemNumber(), m.totalToIssue);
        }
        Map<String, Double> used = new HashMap<>();
        Map<String, Double> rejected = new HashMap<>();
        used.put("106944", 756.27);
        used.put("125112", 751.13);
        used.put("129913", 27038.55);
        rejected.put("106944", 35.0);
        rejected.put("125112", 10.0);
        rejected.put("129913", 120.0);

        List<ReconciliationLine> recon = calc.computeReconciliation(
                progress.cumulativeProducedCartons, issued, used, rejected,
                BatchLotCalculator.DEFAULT_TOLERANCE);

        for (ReconciliationLine line : recon) {
            System.out.println(line);
        }

        boolean ok = calc.allWithinTolerance(recon);
        System.out.println("\nBatch reconciliation status: " +
                (ok ? "WITHIN TOLERANCE - batch can be closed" : "OUT OF TOLERANCE - investigate before closing"));

        System.out.println("\n========== 4) LOT STATUS ALLOCATION (Sealed vs Exhaust) ==========");

        System.out.println("\n---- Bulk Caplets (per WO note: 6CV1111/6CV1191/6CV1201 = Exhaust) ----");
        List<LotBalance> bulkLots = Arrays.asList(
                new LotBalance("6CV1111", LotStatus.OPEN_PARTIAL, 1, 620000, 620000, "Exhaust per WO note"),
                new LotBalance("6CV1191", LotStatus.OPEN_PARTIAL, 2, 580000, 580000, "Exhaust per WO note"),
                new LotBalance("6CV1201", LotStatus.OPEN_PARTIAL, 3, 250000, 250000, "Exhaust per WO note"),
                new LotBalance("6CV1250", LotStatus.SEALED, 4, 500000, 500000, "New sealed lot - top-up only")
        );
        for (LotAllocationResult r : calc.allocateFromLots(hierarchy.caplets, bulkLots)) {
            System.out.println(r);
        }

        System.out.println("\n---- Foil (item 106944) ----");
        double foilTotalRequired = 0;
        for (BatchLotCalculator.MaterialLineResult m : calc.computeMaterialRequirement(cartonsOrdered)) {
            if (m.material.getItemNumber().equals("106944")) foilTotalRequired = m.totalToIssue;
        }
        List<LotBalance> foilLots = Arrays.asList(
                new LotBalance("C22601", LotStatus.OPEN_PARTIAL, 1, 286.70, 500, "Existing partial roll - use first"),
                new LotBalance("H11104", LotStatus.SEALED, 2, 1600.00, 1600, "New roll opened this batch"),
                new LotBalance("H20308", LotStatus.SEALED, 3, 50.00, 50, "Small leftover roll - backup")
        );
        for (LotAllocationResult r : calc.allocateFromLots(foilTotalRequired, foilLots)) {
            System.out.println(r);
        }

        System.out.println("\n========== 5) LOT TYPE - THREE-SHIFT CLASSIFICATION ==========");
        for (LotTypeResult r : calc.classifyLotTypes(cartonsOrdered, log)) {
            System.out.println(r);
        }
    }
}
