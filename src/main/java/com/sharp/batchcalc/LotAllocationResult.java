package com.sharp.batchcalc;

/**
 * Result of allocating a batch's material requirement across a priority-ordered
 * list of lots — how much comes out of each lot, and what its status becomes.
 */
public class LotAllocationResult {
    public final String lotId;
    public final double openingBalance;
    public final double qtyIssued;
    public final double closingBalance;
    public final LotStatus newStatus;
    public final String remarks;

    public LotAllocationResult(String lotId, double openingBalance, double qtyIssued,
                                LotStatus newStatus, String remarks) {
        this.lotId = lotId;
        this.openingBalance = openingBalance;
        this.qtyIssued = qtyIssued;
        this.closingBalance = openingBalance - qtyIssued;
        this.newStatus = newStatus;
        this.remarks = remarks;
    }

    @Override
    public String toString() {
        return String.format("%-10s Opening=%.2f  Issued=%.2f  Closing=%.2f  NewStatus=%-13s %s",
            lotId, openingBalance, qtyIssued, closingBalance, newStatus,
            remarks == null ? "" : remarks);
    }
}
