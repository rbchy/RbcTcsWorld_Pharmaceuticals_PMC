package com.sharp.batchcalc;

/**
 * One BOM/material line (e.g. foil, shipper, carton).
 * উপকরণ চাহিদার একটি লাইন (যেমন: ফয়েল, শিপার, কার্টন)
 *
 * baseQtyPer1000Cartons = PURE usage rate BEFORE overage — set from the route
 * card / BOM. Overage is applied separately via BatchConfig.overagePct.
 */
public class MaterialRequirement {

    private final String itemNumber;
    private final String description;
    private final String unitOfMeasure;
    private final double baseQtyPer1000Cartons;

    public MaterialRequirement(String itemNumber, String description, String unitOfMeasure,
                                double baseQtyPer1000Cartons) {
        this.itemNumber = itemNumber;
        this.description = description;
        this.unitOfMeasure = unitOfMeasure;
        this.baseQtyPer1000Cartons = baseQtyPer1000Cartons;
    }

    public String getItemNumber() { return itemNumber; }
    public String getDescription() { return description; }
    public String getUnitOfMeasure() { return unitOfMeasure; }
    public double getBaseQtyPer1000Cartons() { return baseQtyPer1000Cartons; }

    public double baseRequiredFor(double cartons) {
        return baseQtyPer1000Cartons * cartons / 1000.0;
    }

    public double totalToIssueFor(double cartons, double overagePct) {
        double base = baseRequiredFor(cartons);
        return base * (1 + overagePct);
    }
}
