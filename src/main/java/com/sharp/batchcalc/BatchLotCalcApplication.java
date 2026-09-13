package com.sharp.batchcalc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot entry point for the Batch/Lot Calculator REST API.
 * ব্যাচ/লট ক্যালকুলেটর REST API-এর Spring Boot এন্ট্রি পয়েন্ট
 *
 * Run:  mvn spring-boot:run
 * Then: http://localhost:8081/api/batch/hierarchy  (POST, see README/Postman examples)
 *
 * The plain console demo (Main.java) still works independently via:
 *   mvn compile exec:java -Dexec.mainClass=com.sharp.batchcalc.Main
 */
@SpringBootApplication
public class BatchLotCalcApplication {
    public static void main(String[] args) {
        SpringApplication.run(BatchLotCalcApplication.class, args);
    }
}
