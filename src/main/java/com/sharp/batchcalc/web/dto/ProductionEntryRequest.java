package com.sharp.batchcalc.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public class ProductionEntryRequest {
    @NotNull
    private LocalDate date;   // Jackson parses ISO "2026-07-09" automatically
    @NotBlank
    private String shift;
    private double cartonsProduced;
    private double cartonsRejected;
    private String remarks;

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public String getShift() { return shift; }
    public void setShift(String shift) { this.shift = shift; }
    public double getCartonsProduced() { return cartonsProduced; }
    public void setCartonsProduced(double cartonsProduced) { this.cartonsProduced = cartonsProduced; }
    public double getCartonsRejected() { return cartonsRejected; }
    public void setCartonsRejected(double cartonsRejected) { this.cartonsRejected = cartonsRejected; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}
