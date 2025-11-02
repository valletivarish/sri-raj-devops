package com.lostfound.repository;

import com.lostfound.entity.Item;
import com.lostfound.entity.Item.ItemStatus;
import com.lostfound.entity.Item.ItemType;
import com.lostfound.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ItemRepository extends JpaRepository<Item, Long> {

    @Query("SELECT DISTINCT i FROM Item i LEFT JOIN FETCH i.images WHERE i.softDeleted = false AND (:type IS NULL OR i.type = :type) AND (:status IS NULL OR i.status = :status) AND (:q IS NULL OR lower(i.title) LIKE lower(concat('%', :q, '%')) OR lower(i.description) LIKE lower(concat('%', :q, '%')) OR lower(i.tags) LIKE lower(concat('%', :q, '%'))) ")
    Page<Item> search(@Param("type") ItemType type,
                      @Param("status") ItemStatus status,
                      @Param("q") String q,
                      Pageable pageable);
    
    @Query("SELECT DISTINCT i FROM Item i LEFT JOIN FETCH i.images WHERE i.id = :id AND i.softDeleted = false")
    java.util.Optional<Item> findByIdWithImages(@Param("id") Long id);

    Page<Item> findByPostedByAndSoftDeletedFalse(User postedBy, Pageable pageable);
    
    @Query("SELECT DISTINCT i FROM Item i LEFT JOIN FETCH i.images WHERE i.postedBy = :postedBy AND i.softDeleted = false " +
           "AND (:type IS NULL OR i.type = :type) " +
           "AND (:status IS NULL OR i.status = :status) " +
           "AND (:q IS NULL OR LOWER(i.title) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(i.description) LIKE LOWER(CONCAT('%', :q, '%')))")
    Page<Item> searchByPostedBy(@Param("postedBy") User postedBy,
                                @Param("type") ItemType type,
                                @Param("status") ItemStatus status,
                                @Param("q") String q,
                                Pageable pageable);
    
    java.util.List<Item> findByPostedBy(User postedBy);
}


