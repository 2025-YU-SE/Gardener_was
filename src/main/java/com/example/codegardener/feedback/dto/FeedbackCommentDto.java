package com.example.codegardener.feedback.dto;

import com.example.codegardener.feedback.domain.FeedbackComment;
import com.example.codegardener.user.domain.UserProfile;
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
    private String userName;
    private String userPicture;
    private String content;
    private LocalDateTime createdAt;

    public static FeedbackCommentDto fromEntity(FeedbackComment comment) {
        String userPicture = null;
        UserProfile profile = comment.getUser().getUserProfile();
        if (profile != null) {
            userPicture = profile.getUserPicture();
        }

        return FeedbackCommentDto.builder()
                .commentId(comment.getCommentId())
                .userId(comment.getUser().getUserId())
                .userName(comment.getUser().getUserName())
                .userPicture(userPicture)
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .build();
    }
}