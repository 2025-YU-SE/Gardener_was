package com.example.codegardener.feedback.dto;

import com.example.codegardener.feedback.domain.Feedback;
import com.example.codegardener.user.domain.UserProfile;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeedbackResponseDto {

    private Long feedbackId;
    private Long postId;
    private Long userId;
    private String userName;
    private String userPicture;
    private String content;
    private Double rating;
    private Boolean adoptedTF;
    private Long likesCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static FeedbackResponseDto of(Feedback feedback, long likesCount) {
        String userPicture = null;
        UserProfile profile = feedback.getUser().getUserProfile();
        if (profile != null) {
            userPicture = profile.getUserPicture();
        }

        return FeedbackResponseDto.builder()
                .feedbackId(feedback.getFeedbackId())
                .postId(feedback.getPost().getPostId())
                .userId(feedback.getUser().getUserId())
                .userName(feedback.getUser().getUserName())
                .userPicture(userPicture)
                .content(feedback.getContent())
                .rating(feedback.getRating())
                .adoptedTF(feedback.getAdoptedTF())
                .likesCount(likesCount)
                .createdAt(feedback.getCreatedAt())
                .updatedAt(feedback.getUpdatedAt())
                .build();
    }
}
