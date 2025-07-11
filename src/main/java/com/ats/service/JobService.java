package com.ats.service;

import com.ats.model.Job;
import com.ats.model.User;
import com.ats.repository.JobRepository;
import com.ats.model.ResumeAnalysisResult;
import com.ats.repository.ResumeAnalysisResultRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import com.ats.service.ResumeAnalysisService;

@Service
public class JobService {
    
    @Autowired
    private JobRepository jobRepository;
    
    @Autowired
    private ResumeAnalysisResultRepository resumeAnalysisResultRepository;

    @Autowired
    private ResumeAnalysisService resumeAnalysisService;
    
    public Job createJob(Job job) {
        job.setPostedDate(LocalDateTime.now());
        Job savedJob = jobRepository.save(job);
        // Generate shareable link after job has an ID
        String shareableLink = "https://atsmgmt.onrender.com/jobs/" + savedJob.getId();
        savedJob.setShareableLink(shareableLink);
        return jobRepository.save(savedJob);
    }
    
    public List<Job> getAllJobs() {
        return jobRepository.findAll();
    }
    
    public List<Job> getJobsByEmployer(User employer) {
        return jobRepository.findByEmployer(employer);
    }
    
    public Job getJobById(Long id) {
        return jobRepository.findById(id).orElse(null);
    }

    public Job extractTextFromJobDescription(MultipartFile file) throws IOException {

        String text = extractTextFromEmployerFile(file);
        return resumeAnalysisService.extractTextFromJobDescription(text);
    }

    private String extractTextFromEmployerFile(MultipartFile file) throws IOException {
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

} 
