package com.lostfound.repository;

import com.lostfound.entity.Report;
import com.lostfound.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {
    Page<Report> findByItemPostedBy(User user, Pageable pageable);
    
    @Query("SELECT r FROM Report r WHERE r.item.id IN :itemIds")
    List<Report> findByItemIds(@Param("itemIds") List<Long> itemIds);
}


