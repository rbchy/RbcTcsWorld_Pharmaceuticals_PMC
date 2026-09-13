package com.sharp.batchcalc.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.List;
import java.util.Map;

/** Body for POST /api/batch/reconciliation */
public class ReconciliationRequest {
    @Valid @NotNull
    private BatchConfigRequest config;
    @Valid @NotEmpty
    private List<MaterialRequirementRequest> materials;
    @PositiveOrZero
    private double actualCartonsProduced;

    /** Keyed by item number, e.g. {"106944": 1833.08} */
    private Map<String, Double> qtyIssuedByItem;
    private Map<String, Double> qtyUsedByItem;
    private Map<String, Double> qtyRejectedByItem;

    /** Optional — defaults to BatchLotCalculator.DEFAULT_TOLERANCE (0.03) if omitted */
    private Double tolerance;

    public BatchConfigRequest getConfig() { return config; }
    public void setConfig(BatchConfigRequest config) { this.config = config; }
    public List<MaterialRequirementRequest> getMaterials() { return materials; }
    public void setMaterials(List<MaterialRequirementRequest> materials) { this.materials = materials; }
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
