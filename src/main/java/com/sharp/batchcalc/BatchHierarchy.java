package com.sharp.batchcalc;

/**
 * Full-batch quantity breakdown across the pack hierarchy.
 * সম্পূর্ণ ব্যাচের জন্য প্রতিটি স্তরের পরিমাণ
 */
public class BatchHierarchy {
    public final double cartons;
    public final double bfus;
    public final double caplets;
    public final double bundles;
    public final long shippers;

    public BatchHierarchy(double cartons, double bfus, double caplets, double bundles, long shippers) {
        this.cartons = cartons;
        this.bfus = bfus;
        this.caplets = caplets;
        this.bundles = bundles;
        this.shippers = shippers;
    }

    @Override
    public String toString() {
        return String.format(
            "Cartons=%.1f | BFUs=%.1f | Caplets=%.1f | Bundles=%.1f | Shippers=%d",
            cartons, bfus, caplets, bundles, shippers);
    }
}
