package com.sharp.batchcalc.persistence.dto;

import com.sharp.batchcalc.web.dto.LotBalanceRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;

import java.util.List;

/** Body for POST /api/batch/records/{id}/lot-allocation */
public class PersistLotAllocationRequest {
    @NotBlank
    private String materialItemNumber;   // e.g. "106944", or "BULK_CAPLET" for the drug-substance lots
    @Positive
    private double totalRequirement;
    @Valid @NotEmpty
    private List<LotBalanceRequest> lots;

    public String getMaterialItemNumber() { return materialItemNumber; }
    public void setMaterialItemNumber(String materialItemNumber) { this.materialItemNumber = materialItemNumber; }
    public double getTotalRequirement() { return totalRequirement; }
    public void setTotalRequirement(double totalRequirement) { this.totalRequirement = totalRequirement; }
    public List<LotBalanceRequest> getLots() { return lots; }
    public void setLots(List<LotBalanceRequest> lots) { this.lots = lots; }
}
