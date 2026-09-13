package com.sharp.batchcalc.web.dto;

import com.sharp.batchcalc.LotStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class LotBalanceRequest {
    @NotBlank
    private String lotId;
    @NotNull
    private LotStatus status;   // "SEALED" | "OPEN_PARTIAL" | "EXHAUSTED"
    private int priority;
    private double openingBalance;
    private double nominalQty;
    private String remarks;

    public String getLotId() { return lotId; }
    public void setLotId(String lotId) { this.lotId = lotId; }
    public LotStatus getStatus() { return status; }
    public void setStatus(LotStatus status) { this.status = status; }
    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }
    public double getOpeningBalance() { return openingBalance; }
    public void setOpeningBalance(double openingBalance) { this.openingBalance = openingBalance; }
    public double getNominalQty() { return nominalQty; }
    public void setNominalQty(double nominalQty) { this.nominalQty = nominalQty; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}
