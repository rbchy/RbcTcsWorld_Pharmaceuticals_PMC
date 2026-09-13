package com.sharp.batchcalc;

import java.time.LocalDate;

/**
 * One shift entry classified with its lifecycle stage, plus running totals.
 * একটি শিফট এন্ট্রি, এর লাইফসাইকেল পর্যায় (Startup/Running/Finished)
 */
public class LotTypeResult {
    public final LocalDate date;
    public final String shift;
    public final double cartonsProduced;
    public final double cumulativeProduced;
    public final double outstanding;
    public final LotType lotType;

    public LotTypeResult(LocalDate date, String shift, double cartonsProduced,
                          double cumulativeProduced, double outstanding, LotType lotType) {
        this.date = date;
        this.shift = shift;
        this.cartonsProduced = cartonsProduced;
        this.cumulativeProduced = cumulativeProduced;
        this.outstanding = outstanding;
        this.lotType = lotType;
    }

    @Override
    public String toString() {
        return String.format("%s Shift %-2s Produced=%-7.0f Cumulative=%-8.0f Outstanding=%-8.0f %s",
            date, shift, cartonsProduced, cumulativeProduced, outstanding, lotType);
    }
}
