package com.sharp.batchcalc.persistence.dto;

import jakarta.validation.constraints.PositiveOrZero;

import java.util.Map;

/** Body for POST /api/batch/records/{id}/reconciliation — config/materials are pulled from the saved batch. */
public class PersistReconciliationRequest {
    @PositiveOrZero
    private double actualCartonsProduced;
    private Map<String, Double> qtyIssuedByItem;
    private Map<String, Double> qtyUsedByItem;
    private Map<String, Double> qtyRejectedByItem;
    private Double tolerance; // optional, defaults to BatchLotCalculator.DEFAULT_TOLERANCE

    public double getActualCartonsProduced() { return actualCartonsProduced; }
    public void setActualCartonsProduced(double actualCartonsProduced) { this.actualCartonsProduced = actualCartonsProduced; }
    public Map<String, Double> getQtyIssuedByItem() { return qtyIssuedByItem; }
    public void setQtyIssuedByItem(Map<String, Double> qtyIssuedByItem) { this.qtyIssuedByItem = qtyIssuedByItem; }
    public Map<String, Double> getQtyUsedByItem() { return qtyUsedByItem; }
    public void setQtyUsedByItem(Map<String, Double> qtyUsedByItem) { this.qtyUsedByItem = qtyUsedByItem; }
    public Map<String, Double> getQtyRejectedByItem() { return qtyRejectedByItem; }
    public void setQtyRejectedByItem(Map<String, Double> qtyRejectedByItem) { this.qtyRejectedByItem = qtyRejectedByItem; }
    public Double getTolerance() { return tolerance; }
    public void setTolerance(Double tolerance) { this.tolerance = tolerance; }
}
