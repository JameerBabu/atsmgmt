package com.ats.service;

import com.ats.model.Application;
import com.ats.model.ApplicationStatus;
import com.ats.model.Job;
import com.ats.model.User;
import com.ats.repository.ApplicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import com.ats.model.ResumeAnalysis;
import com.ats.model.ResumeAnalysisResult;
import com.ats.repository.ResumeAnalysisResultRepository;

@Service
public class ApplicationService {
    
    @Autowired
    private ApplicationRepository applicationRepository;
    
    @Autowired
    private ResumeAnalysisService resumeAnalysisService;
    
    @Autowired
    private ResumeAnalysisResultRepository analysisResultRepository;
    
    public Application submitApplication(Application application) throws IOException {
        MultipartFile file = application.getResumeFile();
        if (file != null && !file.isEmpty()) {
            String fileName = file.getOriginalFilename();
            application.setResumeName(fileName);
            application.setResumeType(file.getContentType());
            application.setResumeData(file.getBytes());
            
            // Save application first to get ID
            application.setApplicationDate(LocalDateTime.now());
            application.setStatus(ApplicationStatus.PENDING);
            application = applicationRepository.save(application);
            
            // Extract text and analyze resume
            String resumeText = extractTextFromResume(file);
            ResumeAnalysis analysis = resumeAnalysisService.analyzeResume(resumeText);
            
            // Create and save analysis result
            ResumeAnalysisResult result = new ResumeAnalysisResult();
            result.setApplication(application);
            result.setSkills(String.join(",", analysis.getSkills()));
            result.setYearsOfExperience(analysis.getYearsOfExperience());
            result.setHighestEducation(analysis.getHighestEducation());
            result.setAnalysisDate(LocalDateTime.now());
            analysisResultRepository.save(result);
        }
        
        return application;
    }
    
    private String extractTextFromResume(MultipartFile file) throws IOException {
        String contentType = file.getContentType();
        if (contentType == null) {
            throw new IllegalArgumentException("Content type cannot be null");
        }
        
        byte[] bytes = file.getBytes();
        
        if (contentType.contains("pdf")) {
            try (PDDocument document = PDDocument.load(bytes)) {
                PDFTextStripper stripper = new PDFTextStripper();
                return stripper.getText(document);
            }
        } else if (contentType.contains("docx")) {
            try (XWPFDocument document = new XWPFDocument(file.getInputStream());
            XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
                return extractor.getText();
            }
        } else if (contentType.contains("doc")) {
            try (HWPFDocument document = new HWPFDocument(file.getInputStream());
                 WordExtractor extractor = new WordExtractor(document)) {
                return extractor.getText();
            }
        }
        
        throw new UnsupportedOperationException("Unsupported file type: " + contentType);
    }
    
    public List<Application> getApplicationsByApplicant(User applicant) {
        return applicationRepository.findByApplicant(applicant);
    }
    
    public List<Application> getApplicationsByJob(Job job) {
        return applicationRepository.findByJob(job);
    }
    
    public Application updateApplicationStatus(Long id, ApplicationStatus status) {
        Application application = applicationRepository.findById(id).orElse(null);
        if (application != null) {
            application.setStatus(status);
            return applicationRepository.save(application);
        }
        return null;
    }
    
    public byte[] getResumeData(Long applicationId) {
        Application application = applicationRepository.findById(applicationId)
            .orElseThrow(() -> new RuntimeException("Application not found"));
        return application.getResumeData();
    }
    
    public ResumeAnalysisResult getAnalysisResult(Long applicationId) {
        Application application = applicationRepository.findById(applicationId)
            .orElseThrow(() -> new RuntimeException("Application not found"));
        return analysisResultRepository.findByApplication(application);
    }
    
    public boolean hasAlreadyApplied(Long applicantId, Long jobId) {
        return applicationRepository.existsByApplicantIdAndJobId(applicantId, jobId);
    }
} 