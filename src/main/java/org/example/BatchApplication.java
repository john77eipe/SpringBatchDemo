package org.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Batch Export Application
 * 
 * This application exports data from a database to a CSV file using Spring Batch.
 * It provides a REST API to trigger batch jobs with custom WHERE clauses.
 */
@SpringBootApplication
public class BatchApplication {
    public static void main(String[] args) {
        SpringApplication.run(BatchApplication.class, args);
    }
}