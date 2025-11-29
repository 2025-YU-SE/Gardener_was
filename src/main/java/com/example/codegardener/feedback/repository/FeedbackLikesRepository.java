package com.example.codegardener.feedback.repository;

import com.example.codegardener.feedback.domain.Feedback;
import com.example.codegardener.feedback.domain.FeedbackLike;
import com.example.codegardener.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FeedbackLikesRepository extends JpaRepository<FeedbackLike, Long> {

    // 특정 유저가 해당 피드백에 이미 좋아요를 눌렀는지 확인
    Optional<FeedbackLike> findByUserAndFeedback_FeedbackId(User user, Long feedbackId);
    long countByFeedback(Feedback feedback);
}
