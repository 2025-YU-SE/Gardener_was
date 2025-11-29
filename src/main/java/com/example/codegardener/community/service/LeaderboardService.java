package com.example.codegardener.community.service;

import com.example.codegardener.community.repository.LeaderboardRepository;
import com.example.codegardener.feedback.repository.FeedbackRepository;
import com.example.codegardener.post.repository.PostRepository;
import com.example.codegardener.user.domain.User;
import com.example.codegardener.user.dto.UserResponseDto;
import com.example.codegardener.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LeaderboardService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final FeedbackRepository feedbackRepository;
    private final LeaderboardRepository leaderboardRepository;

    private UserResponseDto convertToDto(User user) {
        long postCount = postRepository.countByUser(user);
        long feedbackCount = feedbackRepository.countByUser(user);
        long adoptedCount = feedbackRepository.countByUserAndAdoptedTF(user, true);
        return UserResponseDto.of(user, postCount, feedbackCount, adoptedCount);
    }

    public List<UserResponseDto> getTop3Leaderboard(String sortBy) {
        return switch (sortBy.toLowerCase()) {
            case "points" -> getTop3UsersByPoints();
            case "weeklyadopted" -> getTop3UsersByWeeklyAdopted();
            case "weeklyfeedback" -> getTop3UsersByWeeklyFeedback();
            default -> throw new IllegalArgumentException("지원하지 않는 정렬 기준입니다: " + sortBy);
        };
    }

    public Page<UserResponseDto> getLeaderboard(String sortBy, Pageable pageable) {
        return switch (sortBy.toLowerCase()) {
            case "points" -> getUsersByPoints(pageable);
            case "weeklyadopted" -> getUsersByWeeklyAdopted(pageable);
            case "weeklyfeedback" -> getUsersByWeeklyFeedback(pageable);
            default -> throw new IllegalArgumentException("지원하지 않는 정렬 기준입니다: " + sortBy);
        };
    }

    // 누적 포인트 TOP3
    public List<UserResponseDto> getTop3UsersByPoints() {
        return userRepository.findTop3ByOrderByUserProfile_PointsDesc().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    // 누적 포인트 페이징
    public Page<UserResponseDto> getUsersByPoints(Pageable pageable) {
        return userRepository.findAllByOrderByUserProfile_PointsDesc(pageable)
                .map(this::convertToDto);
    }

    // 주간 피드백 등록 수 TOP 3
    public List<UserResponseDto> getTop3UsersByWeeklyFeedback() {
        LocalDateTime oneWeekAgo = LocalDateTime.now().minusWeeks(1);
        List<LeaderboardRepository.UserFeedbackCount> topUsersStats = leaderboardRepository.findTop3UsersByFeedbackCount(oneWeekAgo);
        return getUsersInOrderFromStats(topUsersStats);
    }
    // 주간 피드백 등록 수 페이징
    public Page<UserResponseDto> getUsersByWeeklyFeedback(Pageable pageable) {
        LocalDateTime oneWeekAgo = LocalDateTime.now().minusWeeks(1);
        Page<LeaderboardRepository.UserFeedbackCount> statsPage = leaderboardRepository.findUsersByFeedbackCount(oneWeekAgo, pageable);
        List<UserResponseDto> orderedUsers = getUsersInOrderFromStats(statsPage.getContent());
        return new PageImpl<>(orderedUsers, pageable, statsPage.getTotalElements());
    }

    // 주간 피드백 채택 수 TOP 3
    public List<UserResponseDto> getTop3UsersByWeeklyAdopted() {
        LocalDateTime oneWeekAgo = LocalDateTime.now().minusWeeks(1);
        List<LeaderboardRepository.UserFeedbackCount> topUsersStats = leaderboardRepository.findTop3UsersByAdoptedFeedbackCount(oneWeekAgo);
        return getUsersInOrderFromStats(topUsersStats);
    }
    // 주간 피드백 채택 수 페이징
    public Page<UserResponseDto> getUsersByWeeklyAdopted(Pageable pageable) {
        LocalDateTime oneWeekAgo = LocalDateTime.now().minusWeeks(1);
        Page<LeaderboardRepository.UserFeedbackCount> statsPage = leaderboardRepository.findUsersByAdoptedFeedbackCount(oneWeekAgo, pageable);
        List<UserResponseDto> orderedUsers = getUsersInOrderFromStats(statsPage.getContent());
        return new PageImpl<>(orderedUsers, pageable, statsPage.getTotalElements());
    }

    // FeedbackRepository 결과대로 User를 정렬하여 반환하도록 하는 헬퍼 메소드
    private List<UserResponseDto> getUsersInOrderFromStats(List<LeaderboardRepository.UserFeedbackCount> userStats) {
        if (userStats.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> userIds = userStats.stream().map(LeaderboardRepository.UserFeedbackCount::getUserId).toList();
        List<User> users = userRepository.findAllByIdWithProfile(userIds);

        return userIds.stream()
                .flatMap(id -> users.stream().filter(u -> u.getUserId().equals(id)))
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
}