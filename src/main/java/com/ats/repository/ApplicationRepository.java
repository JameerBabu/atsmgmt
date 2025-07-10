package com.ats.repository;

import com.ats.model.Application;
import com.ats.model.Job;
import com.ats.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ApplicationRepository extends JpaRepository<Application, Long> {
    List<Application> findByApplicant(User applicant);
    List<Application> findByJob(Job job);
    Application findByJobAndApplicant(Job job, User applicant);
    boolean existsByApplicantIdAndJobId(Long applicantId, Long jobId);
} 