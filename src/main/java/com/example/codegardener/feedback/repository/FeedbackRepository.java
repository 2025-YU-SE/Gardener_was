package com.example.codegardener.feedback.repository;

import com.example.codegardener.feedback.domain.Feedback;
import com.example.codegardener.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    List<Feedback> findByPost_PostId(Long postId);
    List<Feedback> findByUser(User user);

}