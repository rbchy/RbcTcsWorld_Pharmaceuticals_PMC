package com.sharp.batchcalc.web;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI/Swagger metadata for the interactive docs at /swagger-ui.html.
 * No endpoints here — just descriptive info shown at the top of the docs page.
 */
@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "Pharmaceutical Packaging Batch/Lot Calculator API",
        version = "1.0",
        description = "Batch/lot calculations for pharmaceutical packaging: pack-hierarchy "
            + "breakdown, material requirement, partial-production progress, Sealed-vs-Exhaust "
            + "lot allocation, three-shift (Startup/Running/Finished) classification, and "
            + "material reconciliation (used/rejected/return-unused). "
            + "Two API families: /api/batch/* is stateless (nothing saved); "
            + "/api/batch/records/* persists everything to MySQL.",
        contact = @Contact(name = "RbcTcsWorld_PharmaBatchLotCalc")
    )
)
public class OpenApiConfig {
}
