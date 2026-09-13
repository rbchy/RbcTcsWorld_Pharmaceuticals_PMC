package com.sharp.batchcalc;

/**
 * Lot/container status per BMR/BPR terminology.
 * লট/কন্টেইনারের অবস্থা (BMR/BPR অনুযায়ী)
 *
 * SEALED       — unopened container; full nominal quantity available
 * OPEN_PARTIAL — previously opened/used; remaining balance must be drawn
 *                down before a new Sealed lot is opened
 * EXHAUSTED    — fully consumed; balance is zero, lot is closed
 */
public enum LotStatus {
    SEALED,
    OPEN_PARTIAL,
    EXHAUSTED
}
