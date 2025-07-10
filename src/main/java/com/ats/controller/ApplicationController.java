package com.ats.controller;

import com.ats.model.Application;
import com.ats.model.Job;
import com.ats.model.User;
import com.ats.service.ApplicationService;
import com.ats.service.JobService;
import com.ats.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private UserService userService;

    @Autowired
    private JobService jobService;

    @PostMapping("/submit")
    public ResponseEntity<?> submitApplication(
            @RequestParam("resumeFile") MultipartFile resumeFile,
            @RequestParam("jobId") Long jobId,
            @RequestHeader("Authorization") String token) {
        try {
            // Get applicant from token
            String bearerToken = token.replace("Bearer ", "");
            String userId = bearerToken.replace("dummy-token-", "").split("-")[0];
            User applicant = userService.getUserById(Long.parseLong(userId));

            // Get job
            Job job = jobService.getJobById(jobId);
            if (job == null) {
                return ResponseEntity.badRequest().body("Job not found");
            }

            // Create application
            Application application = new Application();
            application.setApplicant(applicant);
            application.setJob(job);
            application.setResumeFile(resumeFile);

            Application savedApplication = applicationService.submitApplication(application);
            return ResponseEntity.ok(savedApplication);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to submit application: " + e.getMessage());
        }
    }

    @GetMapping("/applicant")
    public ResponseEntity<?> getApplicantApplications(@RequestHeader("Authorization") String token) {
        try {
            String bearerToken = token.replace("Bearer ", "");
            String userId = bearerToken.replace("dummy-token-", "").split("-")[0];
            User applicant = userService.getUserById(Long.parseLong(userId));
            return ResponseEntity.ok(applicationService.getApplicationsByApplicant(applicant));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to fetch applications: " + e.getMessage());
        }
    }
} 