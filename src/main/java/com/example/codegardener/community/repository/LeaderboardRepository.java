package com.example.codegardener.community.repository;

import com.example.codegardener.feedback.domain.Feedback;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LeaderboardRepository extends JpaRepository<Feedback, Long> {

    // JPQL 집계 결과를 담기 위한 인터페이스
    interface UserFeedbackCount {
        Long getUserId();
        Long getCount();
    }

    // 주간 등록 수 TOP3
    @Query("SELECT f.user.userId AS userId, COUNT(f) AS count FROM Feedback f WHERE f.createdAt >= :startDate GROUP BY f.user.userId ORDER BY count DESC LIMIT 3")
    List<UserFeedbackCount> findTop3UsersByFeedbackCount(@Param("startDate") LocalDateTime startDate);

    // 주간 등록 수 페이징
    @Query(value = "SELECT f.user.userId AS userId, COUNT(f) AS count FROM Feedback f WHERE f.createdAt >= :startDate GROUP BY f.user.userId ORDER BY count DESC",
            countQuery = "SELECT COUNT(DISTINCT f.user.userId) FROM Feedback f WHERE f.createdAt >= :startDate")
    Page<UserFeedbackCount> findUsersByFeedbackCount(@Param("startDate") LocalDateTime startDate, Pageable pageable);

    // 주간 채택 수 TOP3
    @Query("SELECT f.user.userId AS userId, COUNT(f) AS count FROM Feedback f WHERE f.adoptedTF = true AND f.createdAt >= :startDate GROUP BY f.user.userId ORDER BY count DESC LIMIT 3")
    List<UserFeedbackCount> findTop3UsersByAdoptedFeedbackCount(@Param("startDate") LocalDateTime startDate);

    // 주간 채택 수 페이징
    @Query(value = "SELECT f.user.userId AS userId, COUNT(f) AS count FROM Feedback f WHERE f.adoptedTF = true AND f.createdAt >= :startDate GROUP BY f.user.userId ORDER BY count DESC",
            countQuery = "SELECT COUNT(DISTINCT f.user.userId) FROM Feedback f WHERE f.adoptedTF = true AND f.createdAt >= :startDate")
    Page<UserFeedbackCount> findUsersByAdoptedFeedbackCount(@Param("startDate") LocalDateTime startDate, Pageable pageable);
}