package org.example.batch.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for the batch export application.
 * Maps properties from application.yml with the 'batch' prefix.
 * Contains settings for database queries, processing parameters, and output configuration.
 */
@ConfigurationProperties(prefix = "batch")
public class BatchProperties {

    /** Number of items to process in each chunk/transaction */
    private int chunkSize;
    
    /** Number of items to fetch in each database page (for JdbcPagingItemReader) */
    private int pageSize = 100;
    
    /** Output configuration for exported files */
    private Output output = new Output();
    
    /** Base SQL query used for data export (SELECT clause) */
    private String baseQuery = "";
    
    /** Default WHERE clause to use if none is provided */
    private String defaultWhereClause = "";

    /**
     * Gets the chunk size for batch processing
     * @return The chunk size
     */
    public int getChunkSize() { return chunkSize; }
    
    /**
     * Sets the chunk size for batch processing
     * @param chunkSize The chunk size to set
     */
    public void setChunkSize(int chunkSize) { this.chunkSize = chunkSize; }

    /**
     * Gets the page size for database pagination
     * @return The page size
     */
    public int getPageSize() { return pageSize; }
    
    /**
     * Sets the page size for database pagination
     * @param pageSize The page size to set
     */
    public void setPageSize(int pageSize) { this.pageSize = pageSize; }

    /**
     * Gets the output configuration
     * @return The output configuration
     */
    public Output getOutput() { return output; }
    
    /**
     * Sets the output configuration
     * @param output The output configuration to set
     */
    public void setOutput(Output output) { this.output = output; }

    /**
     * Gets the base SQL query
     * @return The base SQL query
     */
    public String getBaseQuery() { return baseQuery; }
    
    /**
     * Sets the base SQL query
     * @param baseQuery The base SQL query to set
     */
    public void setBaseQuery(String baseQuery) { this.baseQuery = baseQuery; }

    /**
     * Gets the default WHERE clause
     * @return The default WHERE clause
     */
    public String getDefaultWhereClause() { return defaultWhereClause; }
    
    /**
     * Sets the default WHERE clause
     * @param defaultWhereClause The default WHERE clause to set
     */
    public void setDefaultWhereClause(String defaultWhereClause) { 
        this.defaultWhereClause = defaultWhereClause; 
    }

    /**
     * Extracts the FROM clause from the base query.
     * Handles cases with or without WHERE, GROUP BY, ORDER BY clauses.
     *
     * @return The FROM clause including the FROM keyword and table specification
     * @throws IllegalStateException if the base query doesn't contain a valid FROM clause
     */
    public String extractFromClause() {
        if (baseQuery == null || baseQuery.isEmpty()) {
            throw new IllegalStateException("Base query cannot be null or empty");
        }

        // Find the FROM keyword position (case-insensitive)
        int fromIndex = baseQuery.toLowerCase().indexOf(" from ");
        if (fromIndex == -1) {
            throw new IllegalStateException("Base query must contain FROM clause");
        }

        // Find the first occurrence of a clause that might come after FROM
        String afterFrom = baseQuery.substring(fromIndex + 6);
        String[] endKeywords = {" where ", " group by ", " having ", " order by ", " limit "};

        int endIndex = afterFrom.length();
        for (String keyword : endKeywords) {
            int idx = afterFrom.toLowerCase().indexOf(keyword);
            if (idx != -1 && idx < endIndex) {
                endIndex = idx;
            }
        }

        // Extract and clean the FROM clause
        String fromClause = afterFrom.substring(0, endIndex).trim();
        return "FROM " + fromClause;
    }

    /**
     * Builds a complete SQL query by combining the base query with a WHERE clause.
     * If the provided WHERE clause is null or empty, the default WHERE clause is used.
     * 
     * @param whereClause The WHERE clause to use
     * @return The complete SQL query
     */
    public String buildFullQuery(String whereClause) {
        if (whereClause == null || whereClause.trim().isEmpty()) {
            whereClause = defaultWhereClause;
        }
        return baseQuery + " " + whereClause;
    }

    /**
     * Nested class for output file configuration settings
     */
    public static class Output {
        /** Directory where output files will be written */
        private String directory;
        
        /** Pattern for output filenames, can include {timestamp} token */
        private String filenamePattern;
        
        /** Whether to include a header row in the output file */
        private boolean includeHeader;

        /**
         * Gets the output directory
         * @return The output directory
         */
        public String getDirectory() { return directory; }
        
        /**
         * Sets the output directory
         * @param directory The output directory to set
         */
        public void setDirectory(String directory) { this.directory = directory; }

        /**
         * Gets the filename pattern
         * @return The filename pattern
         */
        public String getFilenamePattern() { return filenamePattern; }
        
        /**
         * Sets the filename pattern
         * @param filenamePattern The filename pattern to set
         */
        public void setFilenamePattern(String filenamePattern) {
            this.filenamePattern = filenamePattern;
        }

        /**
         * Checks if a header row should be included
         * @return true if header should be included, false otherwise
         */
        public boolean isIncludeHeader() { return includeHeader; }
        
        /**
         * Sets whether to include a header row
         * @param includeHeader true to include header, false otherwise
         */
        public void setIncludeHeader(boolean includeHeader) {
            this.includeHeader = includeHeader;
        }
    }
}
