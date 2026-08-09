package com.feel.backend.repository;

import com.feel.backend.entity.CalendarEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CalendarEventRepository extends JpaRepository<CalendarEvent, Long> {

    // 연도별 조회
    @Query("SELECT e FROM CalendarEvent e WHERE YEAR(e.dateStart) = :year OR YEAR(e.dateEnd) = :year ORDER BY e.dateStart ASC")
    List<CalendarEvent> findByYear(@Param("year") int year);

    // 연도와 월별 조회
    @Query("SELECT e FROM CalendarEvent e WHERE " +
           "(YEAR(e.dateStart) = :year AND MONTH(e.dateStart) = :month) OR " +
           "(YEAR(e.dateEnd) = :year AND MONTH(e.dateEnd) = :month) OR " +
           "(e.dateStart <= :startOfMonth AND e.dateEnd >= :endOfMonth) " +
           "ORDER BY e.dateStart ASC")
    List<CalendarEvent> findByYearAndMonth(@Param("year") int year, 
                                            @Param("month") int month,
                                            @Param("startOfMonth") LocalDate startOfMonth,
                                            @Param("endOfMonth") LocalDate endOfMonth);

    // 날짜 범위별 조회
    @Query("SELECT e FROM CalendarEvent e WHERE " +
           "(e.dateStart <= :endDate AND e.dateEnd >= :startDate) " +
           "ORDER BY e.dateStart ASC")
    List<CalendarEvent> findByDateRange(@Param("startDate") LocalDate startDate, 
                                        @Param("endDate") LocalDate endDate);

    // 모든 이벤트 조회 (정렬)
    List<CalendarEvent> findAllByOrderByDateStartAsc();
}
