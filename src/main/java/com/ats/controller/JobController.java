package com.ats.controller;

import com.ats.model.Job;
import com.ats.model.User;
import com.ats.service.JobService;
import com.ats.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import org.springframework.http.HttpStatus;
import com.ats.dto.ApplicationDTO;
import com.ats.service.ApplicationService;
import com.ats.model.Application;
import com.ats.mapper.ApplicationMapper;
import java.util.stream.Collectors;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;
import com.ats.service.SummaryService;
import org.springframework.web.multipart.MultipartFile;
import com.ats.service.ResumeAnalysisService;
import java.io.IOException;
@RestController
@RequestMapping("/api/jobs")
public class JobController {
    
    @Autowired
    private JobService jobService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private ApplicationService applicationService;
    
    @Autowired
    private ApplicationMapper applicationMapper;

    @Autowired
    private SummaryService summaryService;
    
    @Autowired
    private ResumeAnalysisService resumeAnalysisService;
    
    @GetMapping
    public List<Job> getAllJobs() {
        return jobService.getAllJobs();
    }
    
    @PostMapping
    public ResponseEntity<?> createJob(@RequestBody Job job, @RequestHeader("Authorization") String token) {
        try {
            // Extract user ID from token (in production, use proper JWT parsing)
            String bearerToken = token.replace("Bearer ", "");
            String userId = bearerToken.replace("dummy-token-", "").split("-")[0];
            User employer = userService.getUserById(Long.parseLong(userId));
            
            if (employer == null) {
                return ResponseEntity.badRequest().body("Invalid employer");
            }
            
            job.setEmployer(employer);
            Job savedJob = jobService.createJob(job);
            // Return only the shareable link
            return ResponseEntity.ok(savedJob.getShareableLink());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to create job: " + e.getMessage());
        }
    }
    
    @GetMapping("/callback")
    public ResponseEntity<Void> callback() {
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Job> getJobById(@PathVariable Long id) {
        Job job = jobService.getJobById(id);
        if (job != null) {
            return ResponseEntity.ok(job);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/employer")
    public ResponseEntity<?> getEmployerJobs(@RequestHeader("Authorization") String token) {
        try {
            String bearerToken = token.replace("Bearer ", "");
            String userId = bearerToken.replace("dummy-token-", "").split("-")[0];
            User employer = userService.getUserById(Long.parseLong(userId));
            return ResponseEntity.ok(jobService.getJobsByEmployer(employer));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to fetch jobs: " + e.getMessage());
        }
    }

    @GetMapping("/applicant")
    public ResponseEntity<?> getApplicantJobs(@RequestHeader("Authorization") String token) {
        try {
            String bearerToken = token.replace("Bearer ", "");
            String userId = bearerToken.replace("dummy-token-", "").split("-")[0];
            User employer = userService.getUserById(Long.parseLong(userId));
            return ResponseEntity.ok(jobService.getJobsByEmployer(employer));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to fetch jobs: " + e.getMessage());
        }
    }

    @GetMapping("/applications/{jobId}")
    public ResponseEntity<?> getJobApplications(@PathVariable Long jobId, @RequestHeader("Authorization") String token) {
        try {
            String bearerToken = token.replace("Bearer ", "");
            String userId = bearerToken.replace("dummy-token-", "").split("-")[0];
            User employer = userService.getUserById(Long.parseLong(userId));
            
            Job job = jobService.getJobById(jobId);
            if (job == null || !job.getEmployer().getId().equals(employer.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Not authorized to view these applications");
            }
            
            List<Application> applications = applicationService.getApplicationsByJob(job);
            List<ApplicationDTO> dtos = applications.stream()
                .map(applicationMapper::toDTO)
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to fetch applications: " + e.getMessage());
        }
    }

    @GetMapping("/loadSummary")
    public ResponseEntity<?> loadSummary(@RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
    @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(summaryService.getSummaryBetweenDates(startDate, endDate));
    }

    @GetMapping("/downloadSummary")
    public ResponseEntity<?> downloadSummary(@RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
    @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        byte[] pdfBytes = summaryService.generateSummaryPDF(startDate, endDate);
        
        return ResponseEntity.ok()
            .header("Content-Type", "application/pdf")
            .header("Content-Disposition", "attachment; filename=application-summary.pdf")
            .body(pdfBytes);
    }

    @PostMapping("/analyzedescription")
    public ResponseEntity<Void> analyzeJobDescription(@RequestParam("file") MultipartFile file, @RequestHeader("Authorization") String token) {
        try {
            System.out.println("Analyzing job description");
            String bearerToken = token.replace("Bearer ", "");
            String userId = bearerToken.replace("dummy-token-", "").split("-")[0];
            User employer = userService.getUserById(Long.parseLong(userId));

            if (employer == null) {
                return ResponseEntity.badRequest().build();
            }
            Job extractedJob = jobService.extractTextFromJobDescription(file);
            extractedJob.setEmployer(employer);
            // Save the extracted job to database
            jobService.createJob(extractedJob);
            return ResponseEntity.noContent().build();
        } catch (IOException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/upload-resume")
public ResponseEntity<?> uploadResumeForJob(
        @RequestParam("file") MultipartFile file,
        @RequestParam("jobId") Long jobId,
        @RequestHeader("Authorization") String token) {
    try {
        // Extract user ID from token (in production, use proper JWT parsing)
        String bearerToken = token.replace("Bearer ", "");
        String userId = bearerToken.replace("dummy-token-", "").split("-")[0];
        User employer = userService.getUserById(Long.parseLong(userId));

        if (employer == null) {
            return ResponseEntity.badRequest().body("Invalid employer");
        }

        Job job = jobService.getJobById(jobId);
        if (job == null) {
            return ResponseEntity.badRequest().body("Invalid job ID");
        }

        // Create a dummy Application object
        Application application = new Application();
        application.setJob(job);
        application.setApplicant(employer); // or a special user/flag if needed
        // application.setResumeFile(file); -- mysql
        application.setResumeData(file.getBytes());

        applicationService.submitApplication(application);

        return ResponseEntity.noContent().build();
    } catch (Exception e) {
        return ResponseEntity.badRequest().body("Failed to upload resume: " + e.getMessage());
    }
}

    
} 
