package com.example.codegardener.feedback.domain;

import com.example.codegardener.user.domain.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "line_feedback")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LineFeedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long lineFeedbackId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feedback_id", nullable = false)
    private Feedback feedback; // 어떤 피드백에 속하는 라인 피드백인지

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Integer lineNumber; // 피드백 달린 코드 라인 번호

    private Integer endLineNumber; // 여러 줄 범위 가능

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content; // 피드백 내용

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
