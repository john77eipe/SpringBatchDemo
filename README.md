# Database to CSV Export Batch Application

This Spring Boot application uses Spring Batch to export data from a MySQL database to CSV files. 
The application provides a REST API to trigger exports with customizable queries.

## Features

- Uses `JdbcPagingItemReader` for efficient database reading
- Uses `RowMapper` to map database records to Java objects
- Uses `FlatFileItemWriter` for CSV export
- REST API for triggering exports with custom WHERE clauses
- Configurable chunk size, page size, and output settings

## Requirements

- Java 17 or higher
- MySQL database
- Maven

## Application Components

### Core Components

1. **User** - Data model class representing database records
2. **BatchProperties** - Configuration properties for the application
3. **BatchConfig** - Spring Batch configuration for job, step, reader and writer
4. **UserRowMapper** - Maps database rows to User objects
5. **UserFieldExtractor** - Extracts fields from User objects for file output
6. **BatchController** - REST controller for triggering jobs and checking status
7. **JobStarter** - Service for launching batch jobs with parameters

### Architecture Diagram
```md
+------------------+     REST API     +-------------------+
|                  |  POST /export    |                   |
|    Client        |<--------------->|  BatchController   |
| (curl/browser)   |  GET /job/{id}   |                   |
+------------------+                 +-------------------+
|
| launches
v
+----------------------------------------+
|              JobStarter                 |
|  (Manages job execution with params)    |
+----------------------------------------+
|
| configures
v
+------------------------------------------+
|              BatchConfig                  |
| +------------+  +----------+ +----------+ |
| |            |  |          | |          | |
| |  Reader    |->|   Step   |->| Writer  | |
| | (JDBC)     |  |          | | (CSV)    | |
| +------------+  +----------+ +----------+ |
+------------------------------------------+
|
| reads/writes
v
+----------------+                              +-------------+
|                |      reads from             |             |
|  MySQL DB      |<------------------------->  |  CSV Files  |
|                |                             |             |
+----------------+                             +-------------+
```

This diagram shows:
1. Client layer making REST API calls
2. Controller handling HTTP requests
3. JobStarter managing job execution
4. BatchConfig with its components:
    - Reader (JdbcPagingItemReader)
    - Step processing
    - Writer (FlatFileItemWriter)

5. Data flow between MySQL database and CSV files

## Usage

1. To start an export job (POST endpoint):

```shell
# Basic export with no parameters
curl -X POST http://localhost:8080/api/batch/export

# With whereClause parameter
curl -X POST "http://localhost:8080/api/batch/export?whereClause=WHERE%20id%20%3E%2010"

# With filename parameter
curl -X POST "http://localhost:8080/api/batch/export?filename=export.csv"

# With both parameters
curl -X POST "http://localhost:8080/api/batch/export?whereClause=WHERE%20id%20%3E%2010&filename=export.csv"
```

2. To check job status (GET endpoint):

```shell
# Replace 123 with actual job ID returned from the export endpoint
curl -X GET http://localhost:8080/api/batch/job/123
```

Example responses:

```json
{
    "jobId": 123,
    "status": "STARTED",
    "startTime": "2024-01-20T10:00:00.000Z"
}
```
```json
{
    "jobId": 123,
    "status": "COMPLETED",
    "startTime": "2024-01-20T10:00:00.000Z",
    "endTime": "2024-01-20T10:01:00.000Z",
    "exitCode": "COMPLETED",
    "exitDescription": ""
}
```