package com.sharp.batchcalc;

/**
 * One physical lot/container of a material, with its status and balance
 * carried in from stores (or from the previous batch's closing balance).
 * একটি লট/কন্টেইনারের তথ্য — স্ট্যাটাস ও ব্যালেন্স
 */
public class LotBalance {
    private final String lotId;
    private final LotStatus status;
    private final int priority;
    private final double openingBalance;
    private final double nominalQty;
    private final String remarks;

    public LotBalance(String lotId, LotStatus status, int priority, double openingBalance,
                       double nominalQty, String remarks) {
        this.lotId = lotId;
        this.status = status;
        this.priority = priority;
        this.openingBalance = openingBalance;
        this.nominalQty = nominalQty;
        this.remarks = remarks;
    }

    public String getLotId() { return lotId; }
    public LotStatus getStatus() { return status; }
    public int getPriority() { return priority; }
    public double getOpeningBalance() { return openingBalance; }
    public double getNominalQty() { return nominalQty; }
    public String getRemarks() { return remarks; }
}
