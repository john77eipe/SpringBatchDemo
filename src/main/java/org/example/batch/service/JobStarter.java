package org.example.batch.service;

import org.example.config.BatchConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JobStarter implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(JobStarter.class);

    private final JobLauncher jobLauncher;
    private final BatchConfig batchConfig;
    private final JobRepository jobRepository;
    private final BatchConfig.JobCompletionNotificationListener listener;

    @Autowired
    public JobStarter(
            JobLauncher jobLauncher, 
            BatchConfig batchConfig,
            JobRepository jobRepository,
            BatchConfig.JobCompletionNotificationListener listener) {
        this.jobLauncher = jobLauncher;
        this.batchConfig = batchConfig;
        this.jobRepository = jobRepository;
        this.listener = listener;
    }

    @Override
    public void run(String... args) {
        // CommandLineRunner implementation now only logs startup message
        // Actual job execution is controlled by the REST controller
        log.info("Batch Export service is ready. Use the REST API to trigger exports.");
    }

    /**
     * Launches a job with the provided parameters
     */
    public JobExecution launchJob(String whereClause, String filename) {
        try {
            // Create a unique job parameter to allow multiple runs
            JobParameters jobParameters = new JobParametersBuilder()
                    .addDate("run.id", new Date()) // ensures uniqueness
                    .addString("whereClause", whereClause != null ? whereClause : "")
                    .addString("filename", filename != null ? filename : "")
                    .toJobParameters();

            // Create job with the custom parameters
            Job exportJob = batchConfig.exportJob(
                    jobRepository,
                    listener, 
                    whereClause, 
                    filename);

            log.info("Launching export job with whereClause: {}, filename: {}", 
                    whereClause, filename);
            JobExecution execution = jobLauncher.run(exportJob, jobParameters);
            log.info("Job launched with status: {}", execution.getStatus());
            
            return execution;
        } catch (Exception e) {
            log.error("Failed to run export job", e);
            throw new RuntimeException("Failed to run export job", e);
        }
    }
}