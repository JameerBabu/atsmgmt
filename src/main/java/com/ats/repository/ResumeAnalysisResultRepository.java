package com.ats.repository;

import com.ats.model.ResumeAnalysisResult;
import com.ats.model.Application;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResumeAnalysisResultRepository extends JpaRepository<ResumeAnalysisResult, Long> {
    ResumeAnalysisResult findByApplication(Application application);
} 