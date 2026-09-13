package com.sharp.batchcalc.persistence.dto;

import com.sharp.batchcalc.persistence.entity.BatchHeader;

import java.time.Instant;

/** GET /api/batch/records list item — summary only, no nested collections. */
public class BatchSummaryResponse {
    public Long id;
    public String workOrderNo;
    public String itemNumber;
    public String batchLotNo;
    public double cartonsOrdered;
    public Instant createdAt;

    public static BatchSummaryResponse from(BatchHeader h) {
        BatchSummaryResponse r = new BatchSummaryResponse();
        r.id = h.getId();
        r.workOrderNo = h.getWorkOrderNo();
        r.itemNumber = h.getItemNumber();
        r.batchLotNo = h.getBatchLotNo();
        r.cartonsOrdered = h.getCartonsOrdered();
        r.createdAt = h.getCreatedAt();
        return r;
    }
}
