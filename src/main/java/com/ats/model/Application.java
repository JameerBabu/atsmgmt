package com.ats.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import org.springframework.web.multipart.MultipartFile;

@Data
@Entity
@Table(name = "applications")
public class Application {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "job_id")
    private Job job;
    
    @ManyToOne
    @JoinColumn(name = "applicant_id")
    private User applicant;
    
    @Column(name = "resume_name")
    private String resumeName;
    
    @Column(name = "resume_type")
    private String resumeType;
    
    // MySQL
    // @Column(name = "resume_data", columnDefinition = "LONGBLOB")
    // private byte[] resumeData;
    // postgresql
    @Lob
    @Column(name = "resume_data", columnDefinition = "BYTEA")
    private byte[] resumeData;


    private LocalDateTime applicationDate;
    private ApplicationStatus status;
    
    @Transient  // This field won't be persisted in database
    private MultipartFile resumeFile;
} 
