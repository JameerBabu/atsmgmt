package com.ats.service;

import com.ats.model.Job;
import com.ats.model.User;
import com.ats.repository.JobRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;

public class JobServiceTest {

    @Mock
    private JobRepository jobRepository;

    @InjectMocks
    private JobService jobService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createJob_Success() {
        Job job = new Job();
        job.setTitle("Software Engineer");
        job.setDescription("Java Developer position");

        when(jobRepository.save(any(Job.class))).thenReturn(job);

        Job savedJob = jobService.createJob(job);
        assertNotNull(savedJob);
        assertEquals("Software Engineer", savedJob.getTitle());
        assertNotNull(savedJob.getPostedDate());
    }

    @Test
    void getAllJobs_Success() {
        Job job1 = new Job();
        job1.setTitle("Position 1");
        Job job2 = new Job();
        job2.setTitle("Position 2");

        when(jobRepository.findAll()).thenReturn(Arrays.asList(job1, job2));

        List<Job> jobs = jobService.getAllJobs();
        assertEquals(2, jobs.size());
    }

    @Test
    void getJobsByEmployer_Success() {
        User employer = new User();
        Job job = new Job();
        job.setEmployer(employer);

        when(jobRepository.findByEmployer(employer)).thenReturn(Arrays.asList(job));

        List<Job> jobs = jobService.getJobsByEmployer(employer);
        assertEquals(1, jobs.size());
    }
} 