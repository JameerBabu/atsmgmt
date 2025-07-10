package com.ats.controller;

import com.ats.model.Job;
import com.ats.service.JobService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(JobController.class)
public class JobControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JobService jobService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getAllJobs_Success() throws Exception {
        Job job = new Job();
        job.setTitle("Software Engineer");

        when(jobService.getAllJobs()).thenReturn(Arrays.asList(job));

        mockMvc.perform(get("/api/jobs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Software Engineer"));
    }

    @Test
    void createJob_Success() throws Exception {
        Job job = new Job();
        job.setTitle("Software Engineer");

        when(jobService.createJob(any(Job.class))).thenReturn(job);

        mockMvc.perform(post("/api/jobs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(job)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Software Engineer"));
    }

    @Test
    void getJobById_Success() throws Exception {
        Job job = new Job();
        job.setId(1L);
        job.setTitle("Software Engineer");

        when(jobService.getJobById(1L)).thenReturn(job);

        mockMvc.perform(get("/api/jobs/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Software Engineer"));
    }

    @Test
    void getJobById_NotFound() throws Exception {
        when(jobService.getJobById(1L)).thenReturn(null);

        mockMvc.perform(get("/api/jobs/1"))
                .andExpect(status().isNotFound());
    }
} 