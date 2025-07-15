package com.ats.controller;

import com.ats.model.Job;
import com.ats.service.JobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/jobs")
public class PublicJobController {
    @Autowired
    private JobService jobService;

    @GetMapping("/{jobId}")
    public ResponseEntity<Job> getJobDetails(@PathVariable Long jobId) {
        Job job = jobService.getJobById(jobId);
        if (job != null) {
            job.setEmployer(null); // Remove employer details for public view
            return ResponseEntity.ok(job);
        }
        return ResponseEntity.notFound().build();
    }
} 