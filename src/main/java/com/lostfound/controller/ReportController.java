package com.lostfound.controller;

import com.lostfound.entity.Report;
import com.lostfound.service.ReportService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import com.lostfound.dto.ReportDto;

@RestController
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @PostMapping("/api/items/{id}/report")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Report> createReport(@PathVariable("id") Long itemId,
                                               @Valid @RequestBody ReportDto reportDto) {
        Report created = reportService.create(itemId, reportDto.getReporterContact(), reportDto.getReason());
        return ResponseEntity.ok(created);
    }

    @GetMapping("/api/reports")
    @PreAuthorize("hasRole('ADMIN')")
    public Page<Report> listReports(@RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return reportService.list(pageable);
    }

    @GetMapping("/api/reports/my")
    @PreAuthorize("isAuthenticated()")
    public Page<Report> getMyReports(@RequestParam(defaultValue = "0") int page,
                                       @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return reportService.getMyReports(pageable);
    }
}


