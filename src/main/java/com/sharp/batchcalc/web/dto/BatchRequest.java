package com.sharp.batchcalc.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

/** Body for POST /api/batch/hierarchy and POST /api/batch/material-requirement */
public class BatchRequest {
    @Valid @NotNull
    private BatchConfigRequest config;
    @Positive
    private double cartonsOrdered;
    @Valid
    private List<MaterialRequirementRequest> materials; // only needed for material-requirement endpoint

    public BatchConfigRequest getConfig() { return config; }
    public void setConfig(BatchConfigRequest config) { this.config = config; }
    public double getCartonsOrdered() { return cartonsOrdered; }
    public void setCartonsOrdered(double cartonsOrdered) { this.cartonsOrdered = cartonsOrdered; }
    public List<MaterialRequirementRequest> getMaterials() { return materials; }
    public void setMaterials(List<MaterialRequirementRequest> materials) { this.materials = materials; }
}
