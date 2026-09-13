package com.sharp.batchcalc;

/**
 * Result of "how much is left to finish this batch" calculation.
 * ব্যাচ সম্পন্ন করতে আর কতটুকু বাকি — তার হিসাব
 */
public class ProgressResult {
    public final double targetCartons;
    public final double cumulativeProducedCartons;
    public final double cumulativeRejectedCartons;
    public final double remainingCartons;
    public final double percentComplete;
    public final boolean complete;

    public ProgressResult(double targetCartons, double cumulativeProducedCartons,
                           double cumulativeRejectedCartons, double remainingCartons,
                           double percentComplete, boolean complete) {
        this.targetCartons = targetCartons;
        this.cumulativeProducedCartons = cumulativeProducedCartons;
        this.cumulativeRejectedCartons = cumulativeRejectedCartons;
        this.remainingCartons = remainingCartons;
        this.percentComplete = percentComplete;
        this.complete = complete;
    }

    @Override
    public String toString() {
        return String.format(
            "Target=%.0f | Produced=%.0f | Rejected=%.0f | Remaining=%.0f | %.1f%% complete | status=%s",
            targetCartons, cumulativeProducedCartons, cumulativeRejectedCartons, remainingCartons,
            percentComplete * 100, complete ? "COMPLETE" : "IN PROGRESS");
    }
}
