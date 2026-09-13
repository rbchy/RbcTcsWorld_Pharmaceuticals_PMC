package com.sharp.batchcalc;

/**
 * Production-lifecycle stage of a shift entry within one batch.
 * একটি ব্যাচের মধ্যে একটি শিফট এন্ট্রির উৎপাদন-পর্যায়
 *
 * STARTUP  — first shift entry of the batch (line clearance / first-piece
 *            inspection just completed). Occurs exactly once per batch.
 * RUNNING  — normal ongoing production at standard rate, target not yet met.
 * FINISHED — the entry at which cumulative production reaches the target —
 *            batch becomes a Finished Lot, ready for QA release.
 */
public enum LotType {
    STARTUP,
    RUNNING,
    FINISHED
}
