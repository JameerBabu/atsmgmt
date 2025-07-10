package com.ats.dto;

import lombok.Data;

@Data
public class ApplicationStatusSummaryDTO {
    private String status;
    private Long totalApplications;
    private Double percentage;
} 