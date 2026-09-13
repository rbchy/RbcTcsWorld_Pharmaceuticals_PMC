package com.sharp.batchcalc;

import java.time.LocalDate;

/**
 * One shift/day production log entry.
 * একটি শিফট/দিনের উৎপাদন এন্ট্রি
 */
public class ProductionEntry {
    private final LocalDate date;
    private final String shift;
    private final double cartonsProduced;
    private final double cartonsRejected;
    private final String remarks;

    public ProductionEntry(LocalDate date, String shift, double cartonsProduced,
                            double cartonsRejected, String remarks) {
        this.date = date;
        this.shift = shift;
        this.cartonsProduced = cartonsProduced;
        this.cartonsRejected = cartonsRejected;
        this.remarks = remarks;
    }

    public LocalDate getDate() { return date; }
    public String getShift() { return shift; }
    public double getCartonsProduced() { return cartonsProduced; }
    public double getCartonsRejected() { return cartonsRejected; }
    public String getRemarks() { return remarks; }
}
