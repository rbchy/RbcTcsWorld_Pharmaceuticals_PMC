package com.sharp.batchcalc.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;

import java.util.List;

/** Body for POST /api/batch/progress and POST /api/batch/lot-type */
public class ProgressRequest {
    @Positive
    private double targetCartons;
    @Valid @NotEmpty
    private List<ProductionEntryRequest> log;

    public double getTargetCartons() { return targetCartons; }
    public void setTargetCartons(double targetCartons) { this.targetCartons = targetCartons; }
    public List<ProductionEntryRequest> getLog() { return log; }
    public void setLog(List<ProductionEntryRequest> log) { this.log = log; }
}
