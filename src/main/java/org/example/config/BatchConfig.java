package org.example.config;

import org.example.batch.properties.BatchProperties;
import org.example.model.User;
import org.example.utils.UserFieldExtractor;
import org.example.utils.UserRowMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.*;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.*;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;


import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Spring Batch configuration class that defines the batch job components.
 * This class creates and configures the reader, writer, step, and job
 * required for exporting data from database to CSV.
 */
@Configuration
@EnableConfigurationProperties(BatchProperties.class)
public class BatchConfig {

    private static final Logger log = LoggerFactory.getLogger(BatchConfig.class);

    @Autowired
    private DataSource dataSource;

    @Autowired
    private BatchProperties props;

    /**
     * Creates a JdbcPagingItemReader for reading User records from the database.
     * Uses pagination to efficiently process large datasets.
     * 
     * @param whereClause The WHERE clause to filter the database query
     * @return A configured JdbcPagingItemReader instance
     * @throws Exception If there's an error creating the query provider
     */
    @Bean
    @Scope("prototype")
    public JdbcPagingItemReader<User> reader(@Value("${batch.default-where-clause:#{null}}") String whereClause) throws Exception {
        // Define sorting for pagination
        Map<String, Order> sortKeys = new HashMap<>();
        sortKeys.put("id", Order.ASCENDING);
        
        // Create and configure the query provider
        SqlPagingQueryProviderFactoryBean queryProvider = new SqlPagingQueryProviderFactoryBean();
        queryProvider.setDataSource(dataSource);
        queryProvider.setSelectClause(props.getBaseQuery().split("(?i)FROM")[0].trim());
        queryProvider.setFromClause(props.extractFromClause());

        queryProvider.setWhereClause(whereClause != null && !whereClause.isEmpty() ? 
                whereClause : props.getDefaultWhereClause());
        queryProvider.setSortKeys(sortKeys);
        
        // Build and return the reader
        return new JdbcPagingItemReaderBuilder<User>()
                .name("userReader")
                .dataSource(dataSource)
                .queryProvider(queryProvider.getObject())
                .pageSize(props.getPageSize())
                .rowMapper(new UserRowMapper())
                .build();
    }

    @Bean
    @Scope("prototype")
    public FlatFileItemWriter<User> writer(@Value("#{null}") String filename) {
        // Ensure output directory exists
        String dir = props.getOutput().getDirectory();
        if (dir == null || dir.isBlank()) {
            dir = "target";
        }
        
        File directory = new File(dir);
        if (!directory.exists()) {
            boolean created = directory.mkdirs();
            if (!created) {
                throw new IllegalStateException("Failed to create output directory: " + dir);
            }
        }
        
        // Generate filename with timestamp if none provided
        if (filename == null || filename.isEmpty()) {
            String ts = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
            String pattern = props.getOutput().getFilenamePattern();
            filename = pattern.replace("{timestamp}", ts);
        }

        File outputFile = new File(directory, filename);
        // Ensure the file is writable or can be created
        try {
            if (!outputFile.exists()) {
                boolean created = outputFile.createNewFile();
                if (!created) {
                    throw new IllegalStateException("Failed to create output file: " + outputFile);
                }
            }
            if (!outputFile.canWrite()) {
                throw new IllegalStateException("Output file is not writable: " + outputFile);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Error preparing output file: " + outputFile, e);
        }

        // Build and return the writer
        return new FlatFileItemWriterBuilder<User>()
                .name("userWriter")
                .resource(new FileSystemResource(outputFile))
                .delimited()
                .delimiter("\t")
                .fieldExtractor(new UserFieldExtractor())
                .headerCallback(writer -> writer.write("id\tname"))
                .build();
    }

    /**
     * Creates a Step that reads data from database and writes to a file.
     * 
     * @param jobRepository Repository for job execution metadata
     * @param transactionManager Transaction manager for chunk-based processing
     * @param whereClause SQL WHERE clause for filtering data
     * @param filename Output filename
     * @return A configured Step instance
     * @throws Exception If there's an error creating the reader or writer
     */
    @Bean
    @Scope("prototype")
    public Step exportStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            @Value("${batch.default-where-clause:#{null}}") String whereClause,
            @Value("#{null}") String filename
    ) throws Exception {
        return new StepBuilder("export-step", jobRepository)
                .<User, User>chunk(props.getChunkSize(), transactionManager)
                .reader(reader(whereClause))
                .writer(writer(filename))
                .build();
    }

    /**
     * Creates a Job that executes the export step.
     * 
     * @param jobRepository Repository for job execution metadata
     * @param listener Job execution listener for logging
     * @param whereClause SQL WHERE clause for filtering data
     * @param filename Output filename
     * @return A configured Job instance
     * @throws Exception If there's an error creating the step
     */
    @Bean
    @Scope("prototype")
    public Job exportJob(
            JobRepository jobRepository,
            JobCompletionNotificationListener listener,
            @Value("${batch.default-where-clause:#{null}}") String whereClause,
            @Value("#{null}") String filename
    ) throws Exception {
        Step exportStep = exportStep(jobRepository, 
                                    listener.getTransactionManager(), 
                                    whereClause, 
                                    filename);
        
        return new JobBuilder("export-job", jobRepository)
                .listener(listener)
                .start(exportStep)
                .build();
    }

    /**
     * Listener that logs information about job execution status.
     * Also provides access to the transaction manager for the job.
     */
    @Component
    public static class JobCompletionNotificationListener
        extends JobExecutionListenerSupport {
        
        private final PlatformTransactionManager transactionManager;

        /**
         * Creates a new listener with the given transaction manager.
         * 
         * @param transactionManager The transaction manager to use
         */
        public JobCompletionNotificationListener(PlatformTransactionManager transactionManager) {
            this.transactionManager = transactionManager;
        }

        /**
         * Provides access to the transaction manager for the step configuration.
         * 
         * @return The transaction manager
         */
        public PlatformTransactionManager getTransactionManager() {
            return transactionManager;
        }

        /**
         * Called before the job executes.
         * Logs the job name and parameters.
         * 
         * @param jobExecution The current job execution
         */
        @Override
        public void beforeJob(JobExecution jobExecution) {
            log.info("Job starting: {}", jobExecution.getJobInstance().getJobName());
            
            // Log job parameters
            JobParameters params = jobExecution.getJobParameters();
            params.getParameters().forEach((key, value) -> 
                log.info("Job parameter: {} = {}", key, value.getValue()));
        }

        /**
         * Called after the job completes.
         * Logs the job status and any exceptions if it failed.
         * 
         * @param jobExecution The completed job execution
         */
        @Override
        public void afterJob(JobExecution jobExecution) {
            if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
                log.info("Job completed successfully.");
            } else if (jobExecution.getStatus() == BatchStatus.FAILED) {
                log.error("Job failed with exceptions:");
                jobExecution.getAllFailureExceptions()
                    .forEach(ex -> log.error(" ", ex));
        }
    }
    }
}