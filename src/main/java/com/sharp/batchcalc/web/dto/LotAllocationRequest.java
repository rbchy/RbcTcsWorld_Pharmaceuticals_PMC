package com.sharp.batchcalc.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;

import java.util.List;

/** Body for POST /api/batch/lot-allocation */
public class LotAllocationRequest {
    @Positive
    private double totalRequirement;
    @Valid @NotEmpty
    private List<LotBalanceRequest> lots;

    public double getTotalRequirement() { return totalRequirement; }
    public void setTotalRequirement(double totalRequirement) { this.totalRequirement = totalRequirement; }
    public List<LotBalanceRequest> getLots() { return lots; }
    public void setLots(List<LotBalanceRequest> lots) { this.lots = lots; }
}
