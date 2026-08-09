package com.feel.backend.repository;

import com.feel.backend.entity.ActivityCategory;
import com.feel.backend.entity.ActivityPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ActivityPostRepository extends JpaRepository<ActivityPost, Long> {

    /** 마감임박순: end_date 가까운 순, end_date null은 뒤로 */
    @Query("SELECT a FROM ActivityPost a WHERE (:category IS NULL OR a.category = :category) ORDER BY CASE WHEN a.endDate IS NULL THEN 1 ELSE 0 END, a.endDate ASC, a.createdAt DESC")
    Page<ActivityPost> findByCategoryOrderByEndDateAsc(@Param("category") ActivityCategory category, Pageable pageable);

    @Query("SELECT a FROM ActivityPost a WHERE (:category IS NULL OR a.category = :category) ORDER BY a.viewCount DESC, a.createdAt DESC")
    Page<ActivityPost> findByCategoryOrderByViewCountDesc(@Param("category") ActivityCategory category, Pageable pageable);

    @Query("SELECT a FROM ActivityPost a WHERE (:category IS NULL OR a.category = :category) ORDER BY a.createdAt DESC")
    Page<ActivityPost> findByCategoryOrderByCreatedAtDesc(@Param("category") ActivityCategory category, Pageable pageable);
}
