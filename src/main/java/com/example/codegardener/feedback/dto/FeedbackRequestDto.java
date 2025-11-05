package com.example.codegardener.feedback.dto;

import com.example.codegardener.feedback.domain.Feedback;
import com.example.codegardener.post.domain.Post;
import com.example.codegardener.user.domain.User;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeedbackRequestDto {

    private Long postId;
    private String content;
    private Double rating;

    // ✅ 변환 로직은 유지하되, Service에서만 호출할 것
    public Feedback toEntity(User user, Post post) {
        return Feedback.builder()
                .post(post)
                .user(user)
                .content(content == null ? null : content.trim())
                .rating(rating)
                .adoptedTF(false)
                .likesCount(0)
                .build();
    }
}
