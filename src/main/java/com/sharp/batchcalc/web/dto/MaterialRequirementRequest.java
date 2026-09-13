package com.sharp.batchcalc.web.dto;

import jakarta.validation.constraints.NotBlank;

public class MaterialRequirementRequest {
    @NotBlank
    private String itemNumber;
    private String description;
    private String unitOfMeasure;
    private double baseQtyPer1000Cartons;

    public String getItemNumber() { return itemNumber; }
    public void setItemNumber(String itemNumber) { this.itemNumber = itemNumber; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getUnitOfMeasure() { return unitOfMeasure; }
    public void setUnitOfMeasure(String unitOfMeasure) { this.unitOfMeasure = unitOfMeasure; }
    public double getBaseQtyPer1000Cartons() { return baseQtyPer1000Cartons; }
    public void setBaseQtyPer1000Cartons(double baseQtyPer1000Cartons) { this.baseQtyPer1000Cartons = baseQtyPer1000Cartons; }
}
