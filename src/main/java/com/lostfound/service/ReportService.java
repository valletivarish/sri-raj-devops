package com.lostfound.service;

import com.lostfound.entity.Report;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReportService {
    Report create(Long itemId, String reporterContact, String reason);
    Page<Report> list(Pageable pageable);

    Page<Report> getMyReports(Pageable pageable);
}


