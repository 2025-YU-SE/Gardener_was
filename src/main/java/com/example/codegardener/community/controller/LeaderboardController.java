package com.example.codegardener.community.controller;

import com.example.codegardener.community.dto.LeaderboardSummaryDto;
import com.example.codegardener.community.service.LeaderboardService;
import com.example.codegardener.user.dto.UserResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/leaderboard")
public class LeaderboardController {

    private final LeaderboardService leaderboardService;

    @GetMapping("/weekly-stats")
    public ResponseEntity<LeaderboardSummaryDto> getLeaderboardStats() {
        LeaderboardSummaryDto stats = leaderboardService.getWeeklyStats();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/top3")
    public ResponseEntity<List<UserResponseDto>> getTop3Leaderboard(
            @RequestParam(defaultValue = "points") String sortBy) {
        List<UserResponseDto> top3Users = leaderboardService.getTop3Leaderboard(sortBy);
        return ResponseEntity.ok(top3Users);
    }

    @GetMapping
    public ResponseEntity<Page<UserResponseDto>> getLeaderboard(
            @RequestParam(defaultValue = "points") String sortBy,
            Pageable pageable) {
        Page<UserResponseDto> leaderboardPage = leaderboardService.getLeaderboard(sortBy, pageable);
        return ResponseEntity.ok(leaderboardPage);
    }
}