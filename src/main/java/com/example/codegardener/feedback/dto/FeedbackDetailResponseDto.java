package com.example.codegardener.feedback.dto;

import com.example.codegardener.feedback.domain.Feedback;
import com.example.codegardener.user.domain.UserProfile;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeedbackDetailResponseDto {
    private Long feedbackId;
    private Long postId;
    private Long userId;
    private String userName;
    private String userPicture;

    private String content;
    private Double rating;
    private Boolean adoptedTF;
    private Long likesCount;
    private boolean liked;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private List<LineFeedbackDto> lineFeedbacks;
    private List<FeedbackCommentDto> comments;

    public static FeedbackDetailResponseDto of(Feedback feedback, long likesCount, boolean liked) {
        String userPicture = null;
        UserProfile profile = feedback.getUser().getUserProfile();
        if (profile != null) {
            userPicture = profile.getUserPicture();
        }

        return FeedbackDetailResponseDto.builder()
                .feedbackId(feedback.getFeedbackId())
                .postId(feedback.getPost().getPostId())
                .userId(feedback.getUser().getUserId())
                .userName(feedback.getUser().getUserName())
                .userPicture(userPicture)
                .content(feedback.getContent())
                .rating(feedback.getRating())
                .adoptedTF(feedback.getAdoptedTF())
                .likesCount(likesCount)
                .liked(liked)
                .createdAt(feedback.getCreatedAt())
                .updatedAt(feedback.getUpdatedAt())
                .lineFeedbacks(
                        feedback.getLineFeedbackList().stream()
                                .map(LineFeedbackDto::from)
                                .collect(Collectors.toList())
                )
                .comments(
                        feedback.getComments().stream()
                                .map(FeedbackCommentDto::from)
                                .collect(Collectors.toList())
                )
                .build();
    }

}
