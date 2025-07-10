package com.ats.mapper;

import com.ats.model.Application;
import com.ats.dto.ApplicationDTO;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import com.ats.service.ResumeAnalysisService;
import com.ats.model.ResumeAnalysisResult;
import java.util.List;



@Component
public class ApplicationMapper {

    @Autowired
    private ResumeAnalysisService resumeAnalysisService;
    
    public ApplicationDTO toDTO(Application application) {
        ApplicationDTO dto = new ApplicationDTO();
        dto.setId(application.getId());
        dto.setApplicantName(application.getApplicant().getUsername());
        dto.setStatus(application.getStatus().toString());
        dto.setApplicationDate(application.getApplicationDate());
        
        ResumeAnalysisResult analysisResult = resumeAnalysisService.getDetails(application);
        // Get analysis results if they exist
        if (analysisResult != null) {
            dto.setSkills(List.of(analysisResult.getSkills().split(", ")));
            dto.setYearsOfExperience(analysisResult.getYearsOfExperience());
            dto.setHighestEducation(analysisResult.getHighestEducation());
        }
        
        return dto;
    }
} 