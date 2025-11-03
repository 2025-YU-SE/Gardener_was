package com.example.codegardener.post.dto;

import com.example.codegardener.post.domain.Post;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class PostResponseDto {

    private final Long userId;
    private final Long postId;

    private final String title;
    private final String content;

    private final String languages;
    private final String stacks;

    private final String code;
    private final String summary;
    private final Boolean contentsType;

    private final String githubRepoUrl;
    private final String problemStatement;

    private final int likesCount;
    private final int views;
    private final int scrapCount;
    private final int feedbackCount;

    private final LocalDateTime createdAt;
    private final LocalDateTime modifiedAt;

    private final String aiFeedback;

    public static PostResponseDto from(Post post) {
        return PostResponseDto.builder()
                .postId(post.getPostId())
                .userId(post.getUser().getId())
                .title(post.getTitle())
                .content(post.getContent())
                .languages(post.getLangTags())
                .stacks(post.getStackTags())
                .code(post.getCode())
                .summary(post.getSummary())
                .contentsType(post.getContentsType())
                .githubRepoUrl(post.getGithubRepoUrl())
                .problemStatement(post.getProblemStatement())
                .likesCount(post.getLikesCount())
                .views(post.getViews())
                .scrapCount(post.getScrapCount())
                .feedbackCount(post.getFeedbackCount())
                .createdAt(post.getCreatedAt())
                .modifiedAt(post.getModifiedAt())
                .aiFeedback(post.getAiFeedback())
                .build();
    }
}