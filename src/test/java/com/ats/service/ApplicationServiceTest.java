package com.ats.service;

import com.ats.model.Application;
import com.ats.model.ApplicationStatus;
import com.ats.model.User;
import com.ats.repository.ApplicationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;

public class ApplicationServiceTest {

    @Mock
    private ApplicationRepository applicationRepository;

    @InjectMocks
    private ApplicationService applicationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void submitApplication_Success() throws IOException {
        Application application = new Application();
        application.setResumeName("resume.pdf");
        application.setResumeType("application/pdf");

        when(applicationRepository.save(any(Application.class))).thenReturn(application);

        Application savedApplication = applicationService.submitApplication(application);
        assertNotNull(savedApplication);
        assertEquals(ApplicationStatus.PENDING, savedApplication.getStatus());
        assertNotNull(savedApplication.getApplicationDate());
    }

    @Test
    void getApplicationsByApplicant_Success() {
        User applicant = new User();
        Application application = new Application();
        application.setApplicant(applicant);

        when(applicationRepository.findByApplicant(applicant)).thenReturn(Arrays.asList(application));

        List<Application> applications = applicationService.getApplicationsByApplicant(applicant);
        assertEquals(1, applications.size());
    }

    @Test
    void updateApplicationStatus_Success() {
        Application application = new Application();
        application.setId(1L);
        application.setStatus(ApplicationStatus.PENDING);

        when(applicationRepository.findById(1L)).thenReturn(Optional.of(application));
        when(applicationRepository.save(any(Application.class))).thenReturn(application);

        Application updatedApplication = applicationService.updateApplicationStatus(1L, ApplicationStatus.ACCEPTED);
        assertNotNull(updatedApplication);
        assertEquals(ApplicationStatus.ACCEPTED, updatedApplication.getStatus());
    }
} 