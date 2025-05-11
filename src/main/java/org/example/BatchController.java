package org.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/batch")
public class BatchController {

    private static final Logger log = LoggerFactory.getLogger(BatchController.class);
    
    private final JobStarter jobStarter;
    private final JobExplorer jobExplorer;
    
    @Autowired
    public BatchController(JobStarter jobStarter, JobExplorer jobExplorer) {
        this.jobStarter = jobStarter;
        this.jobExplorer = jobExplorer;
    }
    
    @PostMapping("/export")
    public ResponseEntity<Map<String, Object>> startExport(
            @RequestParam(required = false) String whereClause,
            @RequestParam(required = false) String filename) {
        
        try {
            JobExecution execution = jobStarter.launchJob(whereClause, filename);
            
            Map<String, Object> response = new HashMap<>();
            response.put("jobId", execution.getJobId());
            response.put("status", execution.getStatus().toString());
            response.put("startTime", execution.getStartTime());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error starting export job", e);
            Map<String, Object> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @GetMapping("/job/{jobId}")
    public ResponseEntity<Map<String, Object>> getJobStatus(@PathVariable Long jobId) {
        JobExecution jobExecution = jobExplorer.getJobExecution(jobId);
        
        if (jobExecution == null) {
            return ResponseEntity.notFound().build();
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("jobId", jobExecution.getJobId());
        response.put("status", jobExecution.getStatus().toString());
        response.put("startTime", jobExecution.getStartTime());
        response.put("endTime", jobExecution.getEndTime());
        
        if (jobExecution.getExitStatus() != null) {
            response.put("exitCode", jobExecution.getExitStatus().getExitCode());
            response.put("exitDescription", jobExecution.getExitStatus().getExitDescription());
        }
        
        return ResponseEntity.ok(response);
    }
}
