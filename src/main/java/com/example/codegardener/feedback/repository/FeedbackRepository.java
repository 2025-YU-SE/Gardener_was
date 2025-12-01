package com.example.codegardener.feedback.repository;

import com.example.codegardener.feedback.domain.Feedback;
import com.example.codegardener.post.domain.Post;
import com.example.codegardener.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    long countByUser(User user);
    long countByUserAndAdoptedTF(User user, Boolean adoptedTF);
    long countByPost(Post post);

    List<Feedback> findByPost_PostId(Long postId);
    Page<Feedback> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
    List<Feedback> findFirst4ByUserOrderByCreatedAtDesc(User user);

    int countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

}