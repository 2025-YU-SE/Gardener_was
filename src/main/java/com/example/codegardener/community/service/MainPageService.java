package com.example.codegardener.community.service;

import com.example.codegardener.community.dto.MainPageResponseDto;
import com.example.codegardener.feedback.repository.FeedbackRepository;
import com.example.codegardener.post.dto.PostResponseDto;
import com.example.codegardener.post.repository.PostRepository;
import com.example.codegardener.post.service.PostService;
import com.example.codegardener.user.domain.User;
import com.example.codegardener.user.dto.UserResponseDto;
import com.example.codegardener.user.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MainPageService {

    private final PostService postService;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final FeedbackRepository feedbackRepository;
    private final LeaderboardService leaderboardService;

    public MainPageResponseDto getMainPageData(UserDetails userDetails) {
        // 로그인 사용자 정보 조회
        UserResponseDto userInfo = null;
        if (userDetails != null) {
            User user = userRepository.findByUserName(userDetails.getUsername()).orElse(null);
            if (user != null) {
                // 카운트 조회
                long postCount = postRepository.countByUser(user);
                long feedbackCount = feedbackRepository.countByUser(user);
                long adoptedCount = feedbackRepository.countByUserAndAdoptedTF(user, true);

                userInfo = UserResponseDto.of(user, postCount, feedbackCount, adoptedCount);
            }
        }

        List<UserResponseDto> topPointUsers = leaderboardService.getTop3UsersByPoints();

        // PostService를 호출하여 각각의 인기 게시물 목록을 가져옴
        List<PostResponseDto> devPosts = postService.getPopularPosts(true); // true: 개발
        List<PostResponseDto> codingTestPosts = postService.getPopularPosts(false); // false: 코테

        // Builder를 사용하여 DTO를 생성하고 반환
        return MainPageResponseDto.builder()
                .userInfo(userInfo)
                .topPointUsers(topPointUsers)
                .popularDevPosts(devPosts)
                .popularCodingTestPosts(codingTestPosts)
                .build();
    }
}