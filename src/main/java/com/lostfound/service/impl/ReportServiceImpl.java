package com.lostfound.service.impl;

import com.lostfound.entity.Item;
import com.lostfound.entity.Report;
import com.lostfound.entity.User;
import com.lostfound.repository.ItemRepository;
import com.lostfound.repository.ReportRepository;
import com.lostfound.repository.UserRepository;
import com.lostfound.service.ReportService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ReportServiceImpl implements ReportService {

    private final ReportRepository reportRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    public ReportServiceImpl(ReportRepository reportRepository, ItemRepository itemRepository, UserRepository userRepository) {
        this.reportRepository = reportRepository;
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Report create(Long itemId, String reporterContact, String reason) {
        Item item = itemRepository.findById(itemId).orElseThrow(() -> new IllegalArgumentException("Item not found"));
        Report report = new Report();
        report.setItem(item);
        report.setReporterContact(reporterContact);
        report.setReason(reason);
        return reportRepository.save(report);
    }

    @Override
    public Page<Report> list(Pageable pageable) {
        return reportRepository.findAll(pageable);
    }

    @Override
    public Page<Report> getMyReports(Pageable pageable) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = userDetails.getUsername();
        User user = userRepository.findByEmail(username).orElseThrow(() -> new RuntimeException("User not found"));
        return reportRepository.findByItemPostedBy(user, pageable);
    }
}


