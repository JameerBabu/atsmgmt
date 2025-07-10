package com.ats.model;

import lombok.Data;
import java.util.List;

@Data
public class ResumeAnalysis {
    private List<String> skills;
    private String yearsOfExperience;
    private String highestEducation;
    private String location;
} 