package com.ats.dto;

import lombok.Data;
import java.util.List;
import java.time.LocalDateTime;

@Data
public class ApplicationDTO {
    private Long id;
    private String applicantName;
    private List<String> skills;
    private String yearsOfExperience;
    private String highestEducation;
    private LocalDateTime applicationDate;
    private String status;
} 