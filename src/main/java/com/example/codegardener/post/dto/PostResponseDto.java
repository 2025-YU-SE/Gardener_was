package com.example.codegardener.post.dto;

import com.example.codegardener.post.domain.Post;
import com.example.codegardener.user.domain.UserProfile;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class PostResponseDto {

    private final Long postId;
    private final Long userId;
    private final String userName;
    private final String userPicture;

    private final String title;
    private final String content;

    private final String languages;
    private final String stacks;

    private final String code;
    private final String summary;
    private final Boolean contentsType;

    private final String githubRepoUrl;
    private final String problemStatement;

    private final long likesCount;
    private final long scrapCount;
    private final long feedbackCount;
    private final int views;

    private final LocalDateTime createdAt;
    private final LocalDateTime modifiedAt;

    private final String aiFeedback;

    public static PostResponseDto of(Post post, long likesCount, long scrapCount, long feedbackCount){
        String userPicture = null;
        UserProfile profile = post.getUser().getUserProfile();
        if (profile != null) {
            userPicture = profile.getUserPicture();
        }

        return PostResponseDto.builder()
                .postId(post.getPostId())
                .userId(post.getUser().getUserId())
                .userName(post.getUser().getUserName())
                .userPicture(userPicture)
                .title(post.getTitle())
                .content(post.getContent())
                .languages(post.getLangTags())
                .stacks(post.getStackTags())
                .code(post.getCode())
                .summary(post.getSummary())
                .contentsType(post.getContentsType())
                .githubRepoUrl(post.getGithubRepoUrl())
                .problemStatement(post.getProblemStatement())
                .likesCount(likesCount)
                .scrapCount(scrapCount)
                .feedbackCount(feedbackCount)
                .views(post.getViews())
                .createdAt(post.getCreatedAt())
                .modifiedAt(post.getModifiedAt())
                .aiFeedback(post.getAiFeedback())
                .build();
    }
}