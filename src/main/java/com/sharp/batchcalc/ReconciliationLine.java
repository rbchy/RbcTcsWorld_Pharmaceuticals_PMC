package com.sharp.batchcalc;

/**
 * One material's reconciliation line: issued vs. theoretical vs. actual used
 * vs. rejected vs. what must be returned to store as unused.
 * উপকরণের রিকনসিলিয়েশন লাইন
 */
public class ReconciliationLine {
    public final String itemNumber;
    public final String description;
    public final String unitOfMeasure;

    public final double qtyIssued;
    public final double theoreticalRequired;
    public final double qtyActuallyUsed;
    public final double qtyRejected;
    public final double qtyToReturn;
    public final double variancePct;
    public final boolean withinTolerance;

    public ReconciliationLine(String itemNumber, String description, String unitOfMeasure,
                               double qtyIssued, double theoreticalRequired, double qtyActuallyUsed,
                               double qtyRejected, double tolerance) {
        this.itemNumber = itemNumber;
        this.description = description;
        this.unitOfMeasure = unitOfMeasure;
        this.qtyIssued = qtyIssued;
        this.theoreticalRequired = theoreticalRequired;
        this.qtyActuallyUsed = qtyActuallyUsed;
        this.qtyRejected = qtyRejected;
        this.qtyToReturn = qtyIssued - qtyActuallyUsed - qtyRejected;
        this.variancePct = theoreticalRequired == 0 ? 0
                : (qtyActuallyUsed - theoreticalRequired) / theoreticalRequired;
        this.withinTolerance = Math.abs(this.variancePct) <= tolerance;
    }

    @Override
    public String toString() {
        return String.format(
            "%-8s %-45s Issued=%.1f  Theoretical=%.1f  Used=%.1f  Rejected=%.1f  Return=%.1f  Variance=%.2f%%  %s",
            itemNumber, description, qtyIssued, theoreticalRequired, qtyActuallyUsed, qtyRejected,
            qtyToReturn, variancePct * 100, withinTolerance ? "OK" : "OUT OF TOLERANCE");
    }
}
