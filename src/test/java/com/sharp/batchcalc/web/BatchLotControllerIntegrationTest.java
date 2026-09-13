package com.sharp.batchcalc.web;

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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * End-to-end HTTP tests for the STATELESS endpoints (/api/batch/*) — real
 * Spring context, real JSON (de)serialization, real validation, running
 * against an in-memory H2 database (see application-test.properties) so no
 * live MySQL server is needed for `mvn test`.
 *
 * স্টেটলেস endpoint-গুলোর জন্য প্রকৃত HTTP-স্তরের টেস্ট — H2 ইন-মেমোরি DB দিয়ে
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Epic("API Tests — Stateless (/api/batch/*)")
class BatchLotControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private static final String CONFIG_JSON = """
        {
          "config": {
            "workOrderNo": "S180406-1", "itemNumber": "503892",
            "description": "Imodium MSR 24ct caplets", "batchLotNo": "FFC103",
            "customerName": "Kenvue Brands LLC",
            "capletsPerBfu": 6, "bfuPerCarton": 4, "cartonPerBundle": 6, "bundlePerShipper": 6,
            "overagePct": 0.041667
          },
          "cartonsOrdered": 62916
        }
        """;

    @Feature("Hierarchy")
    @Severity(SeverityLevel.CRITICAL)
    @Test
    void hierarchy_matchesWorkOrder() throws Exception {
        mockMvc.perform(post("/api/batch/hierarchy")
                .contentType(MediaType.APPLICATION_JSON)
                .content(CONFIG_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.bfus").value(251664.0))
            .andExpect(jsonPath("$.caplets").value(1509984.0))
            .andExpect(jsonPath("$.shippers").value(1748));
    }

    @Feature("Material Requirement")
    @Severity(SeverityLevel.CRITICAL)
    @Test
    void materialRequirement_cartonMatchesWorkOrder() throws Exception {
        String body = """
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
                {"itemNumber": "129913", "description": "Carton Imodium MSR", "unitOfMeasure": "EA", "baseQtyPer1000Cartons": 1000.00}
              ]
            }
            """;

        mockMvc.perform(post("/api/batch/material-requirement")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].totalToIssue").value(org.hamcrest.Matchers.closeTo(65537.52, 0.01)));
    }

    @Feature("Progress")
    @Severity(SeverityLevel.CRITICAL)
    @Test
    void progress_partialProduction() throws Exception {
        String body = """
            {
              "targetCartons": 62916,
              "log": [
                {"date": "2026-07-09", "shift": "A", "cartonsProduced": 8500, "cartonsRejected": 50, "remarks": ""},
                {"date": "2026-07-09", "shift": "B", "cartonsProduced": 8200, "cartonsRejected": 30, "remarks": ""},
                {"date": "2026-07-10", "shift": "A", "cartonsProduced": 9000, "cartonsRejected": 0, "remarks": ""}
              ]
            }
            """;

        mockMvc.perform(post("/api/batch/progress")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.remainingCartons").value(37216.0))
            .andExpect(jsonPath("$.complete").value(false));
    }

    @Feature("Lot Allocation")
    @Severity(SeverityLevel.CRITICAL)
    @Test
    void lotAllocation_cascadesExhaustThenSealed() throws Exception {
        String body = """
            {
              "totalRequirement": 1509984,
              "lots": [
                {"lotId": "6CV1111", "status": "OPEN_PARTIAL", "priority": 1, "openingBalance": 620000, "nominalQty": 620000},
                {"lotId": "6CV1191", "status": "OPEN_PARTIAL", "priority": 2, "openingBalance": 580000, "nominalQty": 580000},
                {"lotId": "6CV1201", "status": "OPEN_PARTIAL", "priority": 3, "openingBalance": 250000, "nominalQty": 250000},
                {"lotId": "6CV1250", "status": "SEALED", "priority": 4, "openingBalance": 500000, "nominalQty": 500000}
              ]
            }
            """;

        mockMvc.perform(post("/api/batch/lot-allocation")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].newStatus").value("EXHAUSTED"))
            .andExpect(jsonPath("$[3].qtyIssued").value(59984.0))
            .andExpect(jsonPath("$[3].newStatus").value("OPEN_PARTIAL"));
    }

    @Feature("Lot Allocation")
    @Severity(SeverityLevel.CRITICAL)
    @Test
    void lotAllocation_shortfallReturns422() throws Exception {
        String body = """
            {"totalRequirement": 1000, "lots": [
              {"lotId": "LOT-A", "status": "OPEN_PARTIAL", "priority": 1, "openingBalance": 100, "nominalQty": 100}
            ]}
            """;

        mockMvc.perform(post("/api/batch/lot-allocation")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.error").value(org.hamcrest.Matchers.containsString("Shortfall")));
    }

    @Feature("Lot Type")
    @Severity(SeverityLevel.CRITICAL)
    @Test
    void lotType_startupThenRunning() throws Exception {
        String body = """
            {
              "targetCartons": 62916,
              "log": [
                {"date": "2026-07-09", "shift": "A", "cartonsProduced": 8500, "cartonsRejected": 0, "remarks": ""},
                {"date": "2026-07-09", "shift": "B", "cartonsProduced": 8200, "cartonsRejected": 0, "remarks": ""}
              ]
            }
            """;

        mockMvc.perform(post("/api/batch/lot-type")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].lotType").value("STARTUP"))
            .andExpect(jsonPath("$[1].lotType").value("RUNNING"));
    }

    @Feature("Hierarchy")
    @Severity(SeverityLevel.CRITICAL)
    @Test
    void invalidRequest_missingConfig_returns400() throws Exception {
        mockMvc.perform(post("/api/batch/hierarchy")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest());
    }

    // =================================================================
    // ADDITIONAL NEGATIVE / BOUNDARY / SECURITY TESTS
    // (maps to RbcTcsWorld_Test_Cases.xlsx, sheet "2-API Stateless")
    // =================================================================

    // ---------------------------------------------------------------
    // TC-API-S-003 — wrong JSON type for a numeric field
    // ---------------------------------------------------------------
    @Feature("Hierarchy")
    @Severity(SeverityLevel.NORMAL)
    @Test
    void tc_api_s_003_cartonsOrderedWrongType_returns400() throws Exception {
        String body = """
            { "config": { "workOrderNo":"WO","itemNumber":"I","capletsPerBfu":6,"bfuPerCarton":4,
              "cartonPerBundle":6,"bundlePerShipper":6,"overagePct":0.04 },
              "cartonsOrdered": "not-a-number" }
            """;
        mockMvc.perform(post("/api/batch/hierarchy").contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isBadRequest());
    }

    // ---------------------------------------------------------------
    // TC-API-S-004 — @Min(1) violation on a ratio field
    // ---------------------------------------------------------------
    @Feature("Hierarchy")
    @Severity(SeverityLevel.NORMAL)
    @Test
    void tc_api_s_004_capletsPerBfuZero_returns400() throws Exception {
        String body = """
            { "config": { "workOrderNo":"WO","itemNumber":"I","capletsPerBfu":0,"bfuPerCarton":4,
              "cartonPerBundle":6,"bundlePerShipper":6,"overagePct":0.04 },
              "cartonsOrdered": 1000 }
            """;
        mockMvc.perform(post("/api/batch/hierarchy").contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isBadRequest());
    }

    // ---------------------------------------------------------------
    // TC-API-S-005 — malformed JSON
    // ---------------------------------------------------------------
    @Feature("Cross-cutting / Input Handling")
    @Severity(SeverityLevel.NORMAL)
    @Test
    void tc_api_s_005_malformedJson_returns400() throws Exception {
        mockMvc.perform(post("/api/batch/hierarchy")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{cartonsOrdered:62916"))
            .andExpect(status().isBadRequest());
    }

    // ---------------------------------------------------------------
    // TC-API-S-006 — unsupported Content-Type
    // ---------------------------------------------------------------
    @Feature("Cross-cutting / Input Handling")
    @Severity(SeverityLevel.MINOR)
    @Test
    void tc_api_s_006_wrongContentType_returns415() throws Exception {
        mockMvc.perform(post("/api/batch/hierarchy")
                .contentType(MediaType.TEXT_PLAIN)
                .content(CONFIG_JSON))
            .andExpect(status().isUnsupportedMediaType());
    }

    // ---------------------------------------------------------------
    // TC-API-S-008 — empty materials array is VALID for material-requirement
    // (no @NotEmpty on BatchRequest.materials — documents this deliberately)
    // ---------------------------------------------------------------
    @Feature("Material Requirement")
    @Severity(SeverityLevel.NORMAL)
    @Test
    void tc_api_s_008_materialRequirementEmptyMaterialsArray_returns200Empty() throws Exception {
        String body = """
            { "config": { "workOrderNo":"WO","itemNumber":"I","capletsPerBfu":6,"bfuPerCarton":4,
              "cartonPerBundle":6,"bundlePerShipper":6,"overagePct":0.04 },
              "cartonsOrdered": 1000, "materials": [] }
            """;
        mockMvc.perform(post("/api/batch/material-requirement").contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(0));
    }

    // ---------------------------------------------------------------
    // TC-API-S-012 — empty log is VALID for progress (target unmet, not complete)
    // ---------------------------------------------------------------
    @Feature("Progress")
    @Severity(SeverityLevel.NORMAL)
    @Test
    void tc_api_s_012_progressEmptyLog_rejectedByValidation_returns400() throws Exception {
        String body = "{ \"targetCartons\": 1000, \"log\": [] }";
        // NOTE: ProgressRequest.log has @NotEmpty, so an empty array is actually
        // REJECTED — this corrects the test matrix's original assumption that an
        // empty log succeeds at the API layer (it only succeeds when calling the
        // calculator directly, bypassing DTO validation — see TC-UNIT-011).
        mockMvc.perform(post("/api/batch/progress").contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isBadRequest());
    }

    // ---------------------------------------------------------------
    // TC-API-S-013 — @NotBlank cascades into a List<T> element (nested @Valid)
    // ---------------------------------------------------------------
    @Feature("Progress")
    @Severity(SeverityLevel.NORMAL)
    @Test
    void tc_api_s_013_logEntryBlankShift_returns400() throws Exception {
        String body = """
            { "targetCartons": 1000, "log": [
              {"date":"2026-01-01","shift":"","cartonsProduced":100,"cartonsRejected":0,"remarks":""}
            ]}
            """;
        mockMvc.perform(post("/api/batch/progress").contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isBadRequest());
    }

    // ---------------------------------------------------------------
    // TC-API-S-014 — @Positive on targetCartons rejects negative/zero
    // ---------------------------------------------------------------
    @Feature("Progress")
    @Severity(SeverityLevel.NORMAL)
    @Test
    void tc_api_s_014_negativeTargetCartons_returns400() throws Exception {
        String body = """
            { "targetCartons": -500, "log": [
              {"date":"2026-01-01","shift":"A","cartonsProduced":100,"cartonsRejected":0,"remarks":""}
            ]}
            """;
        mockMvc.perform(post("/api/batch/progress").contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isBadRequest());
    }

    // ---------------------------------------------------------------
    // TC-API-S-016 — lot-type: single entry that itself reaches target
    // ---------------------------------------------------------------
    @Feature("Lot Type")
    @Severity(SeverityLevel.NORMAL)
    @Test
    void tc_api_s_016_lotTypeSingleEntryFinishesImmediately() throws Exception {
        String body = """
            { "targetCartons": 1000, "log": [
              {"date":"2026-01-01","shift":"A","cartonsProduced":1000,"cartonsRejected":0,"remarks":""}
            ]}
            """;
        mockMvc.perform(post("/api/batch/lot-type").contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].lotType").value("FINISHED"));
    }

    // ---------------------------------------------------------------
    // TC-API-S-019 — invalid enum value for LotBalanceRequest.status
    // ---------------------------------------------------------------
    @Feature("Lot Allocation")
    @Severity(SeverityLevel.NORMAL)
    @Test
    void tc_api_s_019_invalidEnumStatus_returns400() throws Exception {
        String body = """
            {"totalRequirement": 100, "lots": [
              {"lotId":"L1","status":"BOGUS","priority":1,"openingBalance":100,"nominalQty":100}
            ]}
            """;
        mockMvc.perform(post("/api/batch/lot-allocation").contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isBadRequest());
    }

    // ---------------------------------------------------------------
    // TC-API-S-020 — @NotEmpty on lots
    // ---------------------------------------------------------------
    @Feature("Lot Allocation")
    @Severity(SeverityLevel.NORMAL)
    @Test
    void tc_api_s_020_emptyLotsArray_returns400() throws Exception {
        String body = "{\"totalRequirement\": 100, \"lots\": []}";
        mockMvc.perform(post("/api/batch/lot-allocation").contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isBadRequest());
    }

    // ---------------------------------------------------------------
    // TC-API-S-022 — custom tolerance override changes the flag
    // ---------------------------------------------------------------
    @Feature("Reconciliation")
    @Severity(SeverityLevel.NORMAL)
    @Test
    void tc_api_s_022_reconciliationCustomTolerance_flipsFlag() throws Exception {
        String body = """
            { "config": { "workOrderNo":"WO","itemNumber":"I","capletsPerBfu":6,"bfuPerCarton":4,
              "cartonPerBundle":6,"bundlePerShipper":6,"overagePct":0.0 },
              "materials": [{"itemNumber":"X1","description":"d","unitOfMeasure":"EA","baseQtyPer1000Cartons":100.0}],
              "actualCartonsProduced": 1000,
              "qtyIssuedByItem": {"X1": 100.0},
              "qtyUsedByItem": {"X1": 120.0},
              "qtyRejectedByItem": {"X1": 0.0},
              "tolerance": 0.30
            }
            """;
        // theoretical=100, used=120 -> variance = +20%, fails default 3% tolerance
        // but passes an explicit 30% override
        mockMvc.perform(post("/api/batch/reconciliation").contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].withinTolerance").value(true));
    }

    // ---------------------------------------------------------------
    // TC-API-S-024 — @PositiveOrZero rejects negative actualCartonsProduced
    // ---------------------------------------------------------------
    @Feature("Reconciliation")
    @Severity(SeverityLevel.NORMAL)
    @Test
    void tc_api_s_024_negativeActualCartonsProduced_returns400() throws Exception {
        String body = """
            { "config": { "workOrderNo":"WO","itemNumber":"I","capletsPerBfu":6,"bfuPerCarton":4,
              "cartonPerBundle":6,"bundlePerShipper":6,"overagePct":0.04 },
              "materials": [{"itemNumber":"X1","description":"d","unitOfMeasure":"EA","baseQtyPer1000Cartons":100.0}],
              "actualCartonsProduced": -100
            }
            """;
        mockMvc.perform(post("/api/batch/reconciliation").contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isBadRequest());
    }

    // ---------------------------------------------------------------
    // TC-API-S-025 / 026 — wrong HTTP method / wrong path
    // ---------------------------------------------------------------
    @Feature("Cross-cutting / Input Handling")
    @Severity(SeverityLevel.MINOR)
    @Test
    void tc_api_s_025_wrongHttpMethod_returns405() throws Exception {
        mockMvc.perform(get("/api/batch/hierarchy"))
            .andExpect(status().isMethodNotAllowed());
    }

    @Feature("Cross-cutting / Input Handling")
    @Severity(SeverityLevel.MINOR)
    @Test
    void tc_api_s_026_wrongPathCasing_returns404() throws Exception {
        mockMvc.perform(post("/api/Batch/Hierarchy")
                .contentType(MediaType.APPLICATION_JSON).content(CONFIG_JSON))
            .andExpect(status().isNotFound());
    }

    // ---------------------------------------------------------------
    // TC-API-S-027 — script-tag text field is stored/returned literally, not executed
    // (server-side confirmation only; the UI's own escaping is a separate check)
    // ---------------------------------------------------------------
    @Feature("Security")
    @Severity(SeverityLevel.CRITICAL)
    @Test
    void tc_api_s_027_scriptTagInTextField_storedAsLiteralText() throws Exception {
        String payload = "<script>alert(1)</script>";
        String body = """
            { "config": { "workOrderNo":"%s","itemNumber":"I","capletsPerBfu":6,"bfuPerCarton":4,
              "cartonPerBundle":6,"bundlePerShipper":6,"overagePct":0.04 },
              "cartonsOrdered": 1000 }
            """.formatted(payload.replace("\"", "\\\""));
        mockMvc.perform(post("/api/batch/hierarchy").contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isOk());
        // Hierarchy response doesn't echo workOrderNo back, so this specifically
        // confirms the request is accepted and processed without server-side
        // execution of the payload (no 500, no unexpected behavior change).
    }

    // ---------------------------------------------------------------
    // TC-API-S-029 — very large cartonsOrdered, no overflow/NaN
    // ---------------------------------------------------------------
    @Feature("Hierarchy")
    @Severity(SeverityLevel.MINOR)
    @Test
    void tc_api_s_029_veryLargeCartonsOrdered_noOverflow() throws Exception {
        String body = """
            { "config": { "workOrderNo":"WO","itemNumber":"I","capletsPerBfu":6,"bfuPerCarton":4,
              "cartonPerBundle":6,"bundlePerShipper":6,"overagePct":0.0 },
              "cartonsOrdered": 100000000 }
            """;
        mockMvc.perform(post("/api/batch/hierarchy").contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.bfus").value(400000000.0));
    }
}
