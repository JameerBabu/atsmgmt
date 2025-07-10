package com.ats.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "resume_analysis_results")
public class ResumeAnalysisResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "application_id")
    private Application application;

    @Column(length = 1000)
    private String skills;
    
    private String yearsOfExperience;
    
    private String highestEducation;
    
    private LocalDateTime analysisDate;

    private String location;
} 