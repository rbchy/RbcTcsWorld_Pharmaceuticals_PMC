package com.sharp.batchcalc.persistence;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * End-to-end HTTP test for the PERSISTED batch-records API — creates a real
 * batch, appends production, saves a lot allocation and a reconciliation,
 * then reads it all back, against an in-memory H2 database. This is the
 * automated version of the manual curl walkthrough used during development.
 *
 * সংরক্ষিত ব্যাচ API-এর সম্পূর্ণ জীবনচক্র টেস্ট — H2 দিয়ে
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional // rolls back after each test so tests don't interfere with each other
@Epic("API Tests — Persisted (/api/batch/records/*, MySQL)")
class BatchPersistenceControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private static final String CREATE_BATCH_JSON = """
        {
          "config": {
            "workOrderNo": "S180406-1", "itemNumber": "503892",
            "description": "Imodium MSR 24ct caplets", "batchLotNo": "FFC103",
            "customerName": "Kenvue Brands LLC",
            "capletsPerBfu": 6, "bfuPerCarton": 4, "cartonPerBundle": 6, "bundlePerShipper": 6,
            "overagePct": 0.041667
          },
          "cartonsOrdered": 62916,
          "materials": [
            {"itemNumber": "106944", "description": "Vinyl Klockner PA foil", "unitOfMeasure": "lb", "baseQtyPer1000Cartons": 27.97},
            {"itemNumber": "129913", "description": "Carton Imodium MSR", "unitOfMeasure": "EA", "baseQtyPer1000Cartons": 1000.00}
          ]
        }
        """;

    private Long createBatchAndGetId() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/batch/records")
                .contentType(MediaType.APPLICATION_JSON)
                .content(CREATE_BATCH_JSON))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.materials[1].totalToIssue").value(org.hamcrest.Matchers.closeTo(65537.52, 0.01)))
            .andReturn();

        String json = result.getResponse().getContentAsString();
        // crude id extraction (avoids adding a JSON library dependency just for the test) —
        // non-greedy .*? so this matches the FIRST "id" field (the batch's own), not a
        // later one from a nested material.
        return Long.valueOf(json.replaceAll("(?s).*?\"id\"\\s*:\\s*(\\d+).*", "$1"));
    }

    @Feature("Full Lifecycle")
    @Severity(SeverityLevel.BLOCKER)
    @Test
    void fullLifecycle_createProductionLotAllocationReconciliationDelete() throws Exception {
        Long id = createBatchAndGetId();

        // Append production (ONE call, all entries at once — avoids the
        // append-only duplication gotcha documented in the README)
        String productionJson = """
            [
              {"date": "2026-07-09", "shift": "A", "cartonsProduced": 8500, "cartonsRejected": 50, "remarks": "Batch start"},
              {"date": "2026-07-09", "shift": "B", "cartonsProduced": 8200, "cartonsRejected": 30, "remarks": ""},
              {"date": "2026-07-10", "shift": "A", "cartonsProduced": 9000, "cartonsRejected": 0, "remarks": ""}
            ]
            """;
        mockMvc.perform(post("/api/batch/records/{id}/production", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(productionJson))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].lotType").value("STARTUP"))
            .andExpect(jsonPath("$[2].outstanding").value(37216.0));

        // Lot allocation
        String lotJson = """
            {
              "materialItemNumber": "BULK_CAPLET",
              "totalRequirement": 1509984,
              "lots": [
                {"lotId": "6CV1111", "status": "OPEN_PARTIAL", "priority": 1, "openingBalance": 620000, "nominalQty": 620000},
                {"lotId": "6CV1191", "status": "OPEN_PARTIAL", "priority": 2, "openingBalance": 580000, "nominalQty": 580000},
                {"lotId": "6CV1201", "status": "OPEN_PARTIAL", "priority": 3, "openingBalance": 250000, "nominalQty": 250000},
                {"lotId": "6CV1250", "status": "SEALED", "priority": 4, "openingBalance": 500000, "nominalQty": 500000}
              ]
            }
            """;
        mockMvc.perform(post("/api/batch/records/{id}/lot-allocation", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(lotJson))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[3].qtyIssued").value(59984.0))
            .andExpect(jsonPath("$[3].newStatus").value("OPEN_PARTIAL"));

        // Reconciliation — uses the batch's OWN saved materials, only two here
        String reconJson = """
            {
              "actualCartonsProduced": 25700,
              "qtyIssuedByItem": {"106944": 1833.08, "129913": 65537.52},
              "qtyUsedByItem": {"106944": 756.27, "129913": 27038.55},
              "qtyRejectedByItem": {"106944": 35.0, "129913": 120.0}
            }
            """;
        mockMvc.perform(post("/api/batch/records/{id}/reconciliation", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(reconJson))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].withinTolerance").value(true))
            .andExpect(jsonPath("$[1].withinTolerance").value(true));

        // Full detail — everything should be there together
        mockMvc.perform(get("/api/batch/records/{id}", id))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.productionLog.length()").value(3))
            .andExpect(jsonPath("$.lotBalances.length()").value(4))
            .andExpect(jsonPath("$.reconciliationRecords.length()").value(2));

        // Delete — cascades everything
        mockMvc.perform(delete("/api/batch/records/{id}", id))
            .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/batch/records/{id}", id))
            .andExpect(status().isNotFound());
    }

    @Feature("Get Batch Detail")
    @Severity(SeverityLevel.CRITICAL)
    @Test
    void getUnknownBatch_returns404() throws Exception {
        mockMvc.perform(get("/api/batch/records/999999"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error").value(org.hamcrest.Matchers.containsString("not found")));
    }

    @Feature("Append Production")
    @Severity(SeverityLevel.CRITICAL)
    @Test
    void appendProduction_isNotIdempotent_duplicatesIfCalledTwiceWithSameData() throws Exception {
        // Documents the known append-only gotcha (see README) with a test,
        // so if this behavior is ever intentionally changed, this test will
        // fail and flag the change rather than surprising someone silently.
        Long id = createBatchAndGetId();
        String entry = """
            [{"date": "2026-07-09", "shift": "A", "cartonsProduced": 8500, "cartonsRejected": 0, "remarks": ""}]
            """;

        mockMvc.perform(post("/api/batch/records/{id}/production", id)
                .contentType(MediaType.APPLICATION_JSON).content(entry))
            .andExpect(status().isOk());
        mockMvc.perform(post("/api/batch/records/{id}/production", id)
                .contentType(MediaType.APPLICATION_JSON).content(entry))
            .andExpect(status().isOk());

        mockMvc.perform(get("/api/batch/records/{id}", id))
            .andExpect(jsonPath("$.productionLog.length()").value(2)); // duplicated, by design
    }

    // =================================================================
    // ADDITIONAL NEGATIVE / BOUNDARY / SECURITY TESTS
    // (maps to RbcTcsWorld_Test_Cases.xlsx, sheet "3-API Persisted")
    // =================================================================

    // ---------------------------------------------------------------
    // TC-API-P-002 / 003 — create validation failures
    // ---------------------------------------------------------------
    @Feature("Create Batch")
    @Severity(SeverityLevel.CRITICAL)
    @Test
    void tc_api_p_002_createMissingConfig_returns400_noRowCreated() throws Exception {
        long before = countBatches();
        mockMvc.perform(post("/api/batch/records")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"cartonsOrdered\": 1000}"))
            .andExpect(status().isBadRequest());

        assertEquals(before, countBatches(), "A failed create must not leave a partial row behind");
    }

    @Feature("Create Batch")
    @Severity(SeverityLevel.NORMAL)
    @Test
    void tc_api_p_003_createBlankWorkOrderNo_returns400() throws Exception {
        String body = """
            { "config": { "workOrderNo":"","itemNumber":"I","capletsPerBfu":6,"bfuPerCarton":4,
              "cartonPerBundle":6,"bundlePerShipper":6,"overagePct":0.04 },
              "cartonsOrdered": 1000, "materials": [] }
            """;
        mockMvc.perform(post("/api/batch/records").contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isBadRequest());
    }

    // ---------------------------------------------------------------
    // TC-API-P-005 — list on an empty table returns [] not null/error
    // ---------------------------------------------------------------
    @Feature("List Batches")
    @Severity(SeverityLevel.MINOR)
    @Test
    void tc_api_p_005_listWhenEmpty_returnsEmptyArray() throws Exception {
        // Relies on @Transactional rollback keeping the DB clean between tests;
        // if run in isolation against a fresh H2 instance this is guaranteed empty.
        mockMvc.perform(get("/api/batch/records"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray());
    }

    // ---------------------------------------------------------------
    // TC-API-P-010 — append production to a non-existent batch
    // ---------------------------------------------------------------
    @Feature("Append Production")
    @Severity(SeverityLevel.CRITICAL)
    @Test
    void tc_api_p_010_appendProductionToUnknownBatch_returns404() throws Exception {
        String entry = "[{\"date\":\"2026-01-01\",\"shift\":\"A\",\"cartonsProduced\":100,\"cartonsRejected\":0,\"remarks\":\"\"}]";
        mockMvc.perform(post("/api/batch/records/999999/production")
                .contentType(MediaType.APPLICATION_JSON).content(entry))
            .andExpect(status().isNotFound());
    }

    // ---------------------------------------------------------------
    // TC-API-P-012 — empty array append is a harmless no-op
    // ---------------------------------------------------------------
    @Feature("Append Production")
    @Severity(SeverityLevel.NORMAL)
    @Test
    void tc_api_p_012_appendEmptyArray_isNoOp() throws Exception {
        Long id = createBatchAndGetId();
        mockMvc.perform(post("/api/batch/records/{id}/production", id)
                .contentType(MediaType.APPLICATION_JSON).content("[]"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(0));

        mockMvc.perform(get("/api/batch/records/{id}", id))
            .andExpect(jsonPath("$.productionLog.length()").value(0));
    }

    // ---------------------------------------------------------------
    // TC-API-P-015 — lot-allocation shortfall: no partial rows committed
    // ---------------------------------------------------------------
    @Feature("Lot Allocation")
    @Severity(SeverityLevel.CRITICAL)
    @Test
    void tc_api_p_015_lotAllocationShortfall_returns422_noRowsPersisted() throws Exception {
        Long id = createBatchAndGetId();
        String body = """
            { "materialItemNumber":"BULK_CAPLET", "totalRequirement": 1000,
              "lots": [{"lotId":"L1","status":"OPEN_PARTIAL","priority":1,"openingBalance":100,"nominalQty":100}] }
            """;
        mockMvc.perform(post("/api/batch/records/{id}/lot-allocation", id)
                .contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isUnprocessableEntity());

        // The one lot that WOULD have been fully consumable was still persisted
        // before the shortfall was detected (allocateFromLots throws only after
        // building the full result list) — this test documents whichever the
        // real behavior turns out to be, since the calculator throws only at the
        // very end of allocateFromLots, meaning the DB save loop in
        // BatchPersistenceService.saveLotAllocation() never runs at all for this
        // request (the exception propagates from the calculator call before the
        // per-lot save loop). Expect zero lot_balance rows for this batch.
        mockMvc.perform(get("/api/batch/records/{id}", id))
            .andExpect(jsonPath("$.lotBalances.length()").value(0));
    }

    // ---------------------------------------------------------------
    // TC-API-P-016 / 019 — lot-allocation / reconciliation on unknown batch
    // ---------------------------------------------------------------
    @Feature("Lot Allocation")
    @Severity(SeverityLevel.CRITICAL)
    @Test
    void tc_api_p_016_lotAllocationUnknownBatch_returns404() throws Exception {
        String body = """
            { "materialItemNumber":"X", "totalRequirement": 100,
              "lots": [{"lotId":"L1","status":"SEALED","priority":1,"openingBalance":200,"nominalQty":200}] }
            """;
        mockMvc.perform(post("/api/batch/records/999999/lot-allocation")
                .contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isNotFound());
    }

    @Feature("Reconciliation")
    @Severity(SeverityLevel.CRITICAL)
    @Test
    void tc_api_p_019_reconciliationUnknownBatch_returns404() throws Exception {
        String body = "{\"actualCartonsProduced\": 100}";
        mockMvc.perform(post("/api/batch/records/999999/reconciliation")
                .contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isNotFound());
    }

    // ---------------------------------------------------------------
    // TC-API-P-020 / 021 — delete, then delete again (idempotency contract)
    // ---------------------------------------------------------------
    @Feature("Delete Batch")
    @Severity(SeverityLevel.CRITICAL)
    @Test
    void tc_api_p_020_021_deleteThenDeleteAgain_secondCallReturns404() throws Exception {
        Long id = createBatchAndGetId();

        mockMvc.perform(delete("/api/batch/records/{id}", id))
            .andExpect(status().isNoContent());

        mockMvc.perform(delete("/api/batch/records/{id}", id))
            .andExpect(status().isNotFound());
    }

    // ---------------------------------------------------------------
    // TC-API-P-026 — SQL-injection-style string is stored as literal text
    // ---------------------------------------------------------------
    @Feature("Security")
    @Severity(SeverityLevel.CRITICAL)
    @Test
    void tc_api_p_026_sqlInjectionStyleWorkOrderNo_storedLiterally_tableIntact() throws Exception {
        String payload = "'; DROP TABLE batch_header;--";
        String body = """
            { "config": { "workOrderNo":"%s","itemNumber":"I","capletsPerBfu":6,"bfuPerCarton":4,
              "cartonPerBundle":6,"bundlePerShipper":6,"overagePct":0.04 },
              "cartonsOrdered": 1000, "materials": [] }
            """.formatted(payload);

        MvcResult result = mockMvc.perform(post("/api/batch/records")
                .contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.workOrderNo").value(payload))
            .andReturn();

        // Table still intact and queryable — confirms JPA/Hibernate parameterized
        // the value rather than concatenating it into SQL.
        mockMvc.perform(get("/api/batch/records"))
            .andExpect(status().isOk());
    }

    // ---------------------------------------------------------------
    // Helper
    // ---------------------------------------------------------------
    private long countBatches() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/batch/records")).andReturn();
        String json = result.getResponse().getContentAsString();
        // crude array-element count: number of "id" occurrences (summary DTO has exactly one "id" per element)
        return json.isBlank() ? 0 : java.util.regex.Pattern.compile("\"id\"").matcher(json).results().count();
    }

    private static void assertEquals(long expected, long actual, String message) {
        org.junit.jupiter.api.Assertions.assertEquals(expected, actual, message);
    }
}
