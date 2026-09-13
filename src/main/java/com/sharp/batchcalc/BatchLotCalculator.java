package com.sharp.batchcalc;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Core batch/lot calculator for pharmaceutical packaging operations.
 * ফার্মাসিউটিক্যাল প্যাকেজিং ব্যাচ/লট ক্যালকুলেটর — মূল লজিক
 */
public class BatchLotCalculator {

    /** Default reconciliation tolerance — adjust to your site SOP. */
    public static final double DEFAULT_TOLERANCE = 0.03; // +/-3%

    private final BatchConfig config;
    private final List<MaterialRequirement> materials;

    public BatchLotCalculator(BatchConfig config, List<MaterialRequirement> materials) {
        this.config = config;
        this.materials = materials;
    }

    // ------------------------------------------------------------------
    // 1) START A BATCH — full hierarchy for the ordered quantity
    // ------------------------------------------------------------------
    public BatchHierarchy computeHierarchy(double cartonsOrdered) {
        double bfus = cartonsOrdered * config.getBfuPerCarton();
        double caplets = bfus * config.getCapletsPerBfu();
        double bundles = cartonsOrdered / config.getCartonPerBundle();
        long shippers = (long) Math.ceil(bundles / config.getBundlePerShipper());
        return new BatchHierarchy(cartonsOrdered, bfus, caplets, bundles, shippers);
    }

    public List<MaterialLineResult> computeMaterialRequirement(double cartonsOrdered) {
        List<MaterialLineResult> results = new ArrayList<>();
        for (MaterialRequirement m : materials) {
            double base = m.baseRequiredFor(cartonsOrdered);
            double overage = base * config.getOveragePct();
            double total = base + overage;
            results.add(new MaterialLineResult(m, base, overage, total));
        }
        return results;
    }

    public static class MaterialLineResult {
        public final MaterialRequirement material;
        public final double baseRequired;
        public final double overageQty;
        public final double totalToIssue;

        public MaterialLineResult(MaterialRequirement material, double baseRequired,
                                   double overageQty, double totalToIssue) {
            this.material = material;
            this.baseRequired = baseRequired;
            this.overageQty = overageQty;
            this.totalToIssue = totalToIssue;
        }

        @Override
        public String toString() {
            return String.format("%-8s %-45s base=%.1f  overage=%.1f  TOTAL TO ISSUE=%.1f %s",
                material.getItemNumber(), material.getDescription(), baseRequired, overageQty,
                totalToIssue, material.getUnitOfMeasure());
        }
    }

    // ------------------------------------------------------------------
    // 2) PARTIAL PRODUCTION — remaining to finish the batch
    // ------------------------------------------------------------------
    public ProgressResult computeProgress(double targetCartons, List<ProductionEntry> log) {
        double produced = 0, rejected = 0;
        for (ProductionEntry e : log) {
            produced += e.getCartonsProduced();
            rejected += e.getCartonsRejected();
        }
        double remaining = Math.max(targetCartons - produced, 0);
        double pct = targetCartons == 0 ? 0 : Math.min(produced / targetCartons, 1.0);
        boolean complete = remaining <= 0;
        return new ProgressResult(targetCartons, produced, rejected, remaining, pct, complete);
    }

    public List<MaterialLineResult> computeMaterialToFinish(ProgressResult progress) {
        return computeMaterialRequirement(progress.remainingCartons);
    }

    // ------------------------------------------------------------------
    // 3) RECONCILIATION — used / rejected / unused-to-return per material
    // ------------------------------------------------------------------
    public List<ReconciliationLine> computeReconciliation(
            double actualCartonsProduced,
            Map<String, Double> qtyIssuedByItem,
            Map<String, Double> qtyUsedByItem,
            Map<String, Double> qtyRejectedByItem,
            double tolerance) {

        List<ReconciliationLine> lines = new ArrayList<>();
        for (MaterialRequirement m : materials) {
            double theoretical = m.totalToIssueFor(actualCartonsProduced, config.getOveragePct());
            double issued = qtyIssuedByItem.getOrDefault(m.getItemNumber(), 0.0);
            double used = qtyUsedByItem.getOrDefault(m.getItemNumber(), theoretical);
            double rejected = qtyRejectedByItem.getOrDefault(m.getItemNumber(), 0.0);
            lines.add(new ReconciliationLine(m.getItemNumber(), m.getDescription(), m.getUnitOfMeasure(),
                    issued, theoretical, used, rejected, tolerance));
        }
        return lines;
    }

    public boolean allWithinTolerance(List<ReconciliationLine> lines) {
        return lines.stream().allMatch(l -> l.withinTolerance);
    }

    // ------------------------------------------------------------------
    // 4) LOT STATUS ALLOCATION — Sealed vs Exhaust (per BMR/BPR)
    // ------------------------------------------------------------------
    public List<LotAllocationResult> allocateFromLots(double totalRequirement, List<LotBalance> lots) {
        List<LotBalance> ordered = new ArrayList<>(lots);
        ordered.sort((a, b) -> Integer.compare(a.getPriority(), b.getPriority()));

        List<LotAllocationResult> results = new ArrayList<>();
        double cumulativeIssued = 0;
        for (LotBalance lot : ordered) {
            double remainingNeed = Math.max(totalRequirement - cumulativeIssued, 0);
            double issued = Math.min(lot.getOpeningBalance(), remainingNeed);
            double closing = lot.getOpeningBalance() - issued;
            LotStatus newStatus = closing <= 0
                    ? LotStatus.EXHAUSTED
                    : (issued > 0 ? LotStatus.OPEN_PARTIAL : LotStatus.SEALED);
            results.add(new LotAllocationResult(lot.getLotId(), lot.getOpeningBalance(), issued,
                    newStatus, lot.getRemarks()));
            cumulativeIssued += issued;
        }

        double totalIssued = results.stream().mapToDouble(r -> r.qtyIssued).sum();
        if (totalIssued + 1e-6 < totalRequirement) {
            throw new IllegalStateException(String.format(
                "Shortfall: lots provide %.2f but %.2f is required — add another sealed lot "
                + "(ঘাটতি: লটগুলোতে %.2f আছে কিন্তু %.2f প্রয়োজন — আরেকটি সিলড লট যোগ করুন)",
                totalIssued, totalRequirement, totalIssued, totalRequirement));
        }
        return results;
    }

    // ------------------------------------------------------------------
    // 5) LOT TYPE CLASSIFICATION — Startup / Running / Finished (per shift)
    // ------------------------------------------------------------------
    public List<LotTypeResult> classifyLotTypes(double targetCartons, List<ProductionEntry> log) {
        List<LotTypeResult> results = new ArrayList<>();
        double cumulative = 0;
        for (int i = 0; i < log.size(); i++) {
            ProductionEntry e = log.get(i);
            cumulative += e.getCartonsProduced();
            double outstanding = Math.max(targetCartons - cumulative, 0);
            LotType type;
            if (outstanding <= 0) {
                type = LotType.FINISHED;
            } else if (i == 0) {
                type = LotType.STARTUP;
            } else {
                type = LotType.RUNNING;
            }
            results.add(new LotTypeResult(e.getDate(), e.getShift(), e.getCartonsProduced(),
                    cumulative, outstanding, type));
            if (type == LotType.FINISHED) break;
        }
        return results;
    }
}
