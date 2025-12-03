package com.example.codegardener.community.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LeaderboardSummaryDto {
    private long newUsersCount;
    private long newPostsCount;
    private long newFeedbacksCount;
}