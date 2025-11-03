package com.example.codegardener.post.dto;

import com.example.codegardener.post.domain.Post;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PostResponseDto {

    private Long userId;
    private Long postId;

    private String title;
    private String content;

    private String languages;
    private String stacks;

    private String code;
    private String summary;
    private Boolean contentsType;

    private String githubRepoUrl;
    private String problemStatement;

    private int likesCount;
    private int views;
    private int scrapCount;
    private int feedbackCount;

    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

    private String aiFeedback;

    public static PostResponseDto from(Post post) {
        PostResponseDto dto = new PostResponseDto();
        dto.setPostId(post.getPostId());
        dto.setUserId(post.getUser().getId());

        dto.setTitle(post.getTitle());
        dto.setContent(post.getContent());

        dto.setLanguages(post.getLangTags());
        dto.setStacks(post.getStackTags());

        dto.setCode(post.getCode());
        dto.setSummary(post.getSummary());
        dto.setContentsType(post.getContentsType());

        dto.setGithubRepoUrl(post.getGithubRepoUrl());
        dto.setProblemStatement(post.getProblemStatement());

        dto.setLikesCount(post.getLikesCount());
        dto.setViews(post.getViews());
        dto.setScrapCount(post.getScrapCount());
        dto.setFeedbackCount(post.getFeedbackCount());

        dto.setCreatedAt(post.getCreatedAt());
        dto.setModifiedAt(post.getModifiedAt());

        dto.setAiFeedback(post.getAiFeedback());

        return dto;
    }
}