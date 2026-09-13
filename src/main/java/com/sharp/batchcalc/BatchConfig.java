package com.sharp.batchcalc;

/**
 * Pack-hierarchy configuration for one batch/work order.
 * Kaplet -> BFU -> Carton -> Bundle -> Shipper
 * (ক্যাপলেট -> BFU -> কার্টন -> বান্ডেল -> শিপার)
 *
 * Example (from Sharp Packaging work order S180406-1):
 *   6 caplets/BFU; 4 BFUs/carton; 6 cartons/bundle; 6 bundles/shipper
 */
public class BatchConfig {

    private final String workOrderNo;
    private final String itemNumber;
    private final String description;
    private final String batchLotNo;
    private final String customerName;

    private final int capletsPerBfu;
    private final int bfuPerCarton;
    private final int cartonPerBundle;
    private final int bundlePerShipper;

    private final double overagePct;

    public BatchConfig(String workOrderNo, String itemNumber, String description, String batchLotNo,
                        String customerName, int capletsPerBfu, int bfuPerCarton, int cartonPerBundle,
                        int bundlePerShipper, double overagePct) {
        this.workOrderNo = workOrderNo;
        this.itemNumber = itemNumber;
        this.description = description;
        this.batchLotNo = batchLotNo;
        this.customerName = customerName;
        this.capletsPerBfu = capletsPerBfu;
        this.bfuPerCarton = bfuPerCarton;
        this.cartonPerBundle = cartonPerBundle;
        this.bundlePerShipper = bundlePerShipper;
        this.overagePct = overagePct;
    }

    public String getWorkOrderNo() { return workOrderNo; }
    public String getItemNumber() { return itemNumber; }
    public String getDescription() { return description; }
    public String getBatchLotNo() { return batchLotNo; }
    public String getCustomerName() { return customerName; }
    public int getCapletsPerBfu() { return capletsPerBfu; }
    public int getBfuPerCarton() { return bfuPerCarton; }
    public int getCartonPerBundle() { return cartonPerBundle; }
    public int getBundlePerShipper() { return bundlePerShipper; }
    public double getOveragePct() { return overagePct; }
}
