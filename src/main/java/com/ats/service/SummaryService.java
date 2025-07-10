package com.ats.service;

import com.ats.dto.ApplicationStatusSummaryDTO;
import java.time.LocalDate;
import java.util.List;

public interface SummaryService {
    List<ApplicationStatusSummaryDTO> getSummaryBetweenDates(LocalDate startDate, LocalDate endDate);
    byte[] generateSummaryPDF(LocalDate startDate, LocalDate endDate);
}

