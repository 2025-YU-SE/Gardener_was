package com.example.codegardener.user.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.codegardener.user.domain.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUserName(String userName);
    Optional<User> findByEmail(String email);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.userProfile up ORDER BY up.points DESC")
    List<User> findTop3ByOrderByUserProfile_PointsDesc();

    @Query(value = "SELECT u FROM User u LEFT JOIN FETCH u.userProfile up ORDER BY up.points DESC",
            countQuery = "SELECT count(u) FROM User u")
    Page<User> findAllByOrderByUserProfile_PointsDesc(Pageable pageable);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.userProfile WHERE u.userId IN :userIds")
    List<User> findAllByIdWithProfile(@Param("userIds") List<Long> userIds);

    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}