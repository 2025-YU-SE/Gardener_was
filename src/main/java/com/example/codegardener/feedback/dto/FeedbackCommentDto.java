package com.example.codegardener.feedback.dto;

import com.example.codegardener.feedback.domain.FeedbackComment;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeedbackCommentDto {
    private Long commentId;
    private Long userId;
    private String content;
    private LocalDateTime createdAt;

    public static FeedbackCommentDto fromEntity(FeedbackComment comment) {
        return FeedbackCommentDto.builder()
                .commentId(comment.getCommentId())
                .userId(comment.getUser().getId())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .build();
    }
}
