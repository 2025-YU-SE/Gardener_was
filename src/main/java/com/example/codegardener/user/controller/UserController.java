package com.example.codegardener.user.controller;

import com.example.codegardener.feedback.dto.FeedbackResponseDto;
import com.example.codegardener.feedback.service.FeedbackService;
import com.example.codegardener.post.dto.PostResponseDto;
import com.example.codegardener.post.service.PostService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;

import lombok.RequiredArgsConstructor;

import com.example.codegardener.user.dto.LoginRequestDto;
import com.example.codegardener.user.dto.LoginResponseDto;
import com.example.codegardener.user.dto.SignUpRequestDto;
import com.example.codegardener.user.dto.UserResponseDto;
import com.example.codegardener.user.service.UserService;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;
    private final PostService postService;
    private final FeedbackService feedbackService;

    @PostMapping("/signup")
    public ResponseEntity<UserResponseDto> signUp(@RequestBody SignUpRequestDto signUpRequestDto) {
        UserResponseDto userResponseDto = userService.signUp(signUpRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(userResponseDto);
    }

    @GetMapping("/check-username")
    public ResponseEntity<Boolean> checkUserName(@RequestParam String userName) {
        boolean isAvailable = userService.isUserNameAvailable(userName);
        return ResponseEntity.ok(isAvailable);
    }

    @GetMapping("/check-email")
    public ResponseEntity<Boolean> checkEmail(@RequestParam String email) {
        boolean isAvailable = userService.isEmailAvailable(email);
        return ResponseEntity.ok(isAvailable);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody LoginRequestDto loginRequestDto) {
        String token = userService.login(loginRequestDto);
        return ResponseEntity.ok(new LoginResponseDto(token));
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            String token = bearerToken.substring(7);
            userService.logout(token);
            return ResponseEntity.ok("로그아웃 되었습니다.");
        }
        return ResponseEntity.badRequest().body("토큰이 없습니다.");
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserResponseDto> getUserProfile(@PathVariable Long userId) {
        UserResponseDto userResponseDto = userService.getUserProfile(userId);
        return ResponseEntity.ok(userResponseDto);
    }

    @PostMapping("/attendance")
    public ResponseEntity<String> checkAttendance(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }

        try {
            String message = userService.recordAttendance(userDetails.getUsername());
            return ResponseEntity.ok(message);
        } catch (IllegalArgumentException e) { // 사용자를 찾을 수 없는 경우 등
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) { // 그 외 서버 오류
            log.error("Error recording attendance for user: {}", userDetails.getUsername(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("출석 처리 중 오류가 발생했습니다.");
        }
    }


    @DeleteMapping("/me")
    public ResponseEntity<String> deleteMyAccount(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("인증이 필요합니다.");
        }

        try {
            userService.deleteCurrentUser(userDetails.getUsername());
            return ResponseEntity.ok("회원 탈퇴가 완료되었습니다.");
        } catch (IllegalStateException e) { // 관리자 탈퇴 시도 등
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) { // 그 외 예외 처리
            log.error("Error deleting user account: {}", userDetails.getUsername(), e); // 로그 추가
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("회원 탈퇴 중 오류가 발생했습니다.");
        }
    }

    @PutMapping(value = "/profile-picture", consumes = "multipart/form-data")
    public ResponseEntity<String> updateProfilePicture(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }

        try {
            String imageUrl = userService.updateProfilePicture(userDetails.getUsername(), file);
            return ResponseEntity.ok(imageUrl); // 변경된 이미지 URL 반환
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/profile-picture")
    public ResponseEntity<String> deleteProfilePicture(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }

        userService.deleteProfilePicture(userDetails.getUsername());
        return ResponseEntity.ok("프로필 사진이 기본 이미지로 변경되었습니다.");
    }

    @GetMapping("/{userId}/posts/recent")
    public ResponseEntity<List<PostResponseDto>> getUserRecentPosts(
            @PathVariable Long userId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String currentUsername = (userDetails != null) ? userDetails.getUsername() : null;
        List<PostResponseDto> posts = postService.getRecentPostsByUserId(userId, currentUsername);
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/{userId}/posts")
    public ResponseEntity<Page<PostResponseDto>> getUserPosts(
            @PathVariable Long userId,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String currentUsername = (userDetails != null) ? userDetails.getUsername() : null;
        Page<PostResponseDto> postPage = postService.getPostsByUserId(userId, pageable, currentUsername);
        return ResponseEntity.ok(postPage);
    }

    @GetMapping("/{userId}/feedbacks/recent")
    public ResponseEntity<List<FeedbackResponseDto>> getUserRecentFeedbacks(
            @PathVariable Long userId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String currentUsername = (userDetails != null) ? userDetails.getUsername() : null;
        List<FeedbackResponseDto> feedbacks = feedbackService.getRecentFeedbacksByUserId(userId, currentUsername);
        return ResponseEntity.ok(feedbacks);
    }

    @GetMapping("/{userId}/feedbacks")
    public ResponseEntity<Page<FeedbackResponseDto>> getUserFeedbacks(
            @PathVariable Long userId,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String currentUsername = (userDetails != null) ? userDetails.getUsername() : null;
        Page<FeedbackResponseDto> feedbackPage = feedbackService.getFeedbacksByUserId(userId, pageable, currentUsername);
        return ResponseEntity.ok(feedbackPage);
    }

    @GetMapping("/{userId}/scraps/recent")
    public ResponseEntity<List<PostResponseDto>> getMyRecentScraps(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        if (userDetails == null) throw new AccessDeniedException("로그인이 필요합니다.");
        List<PostResponseDto> scraps = postService.getRecentScrappedPostsByUsername(userDetails.getUsername());
        return ResponseEntity.ok(scraps);
    }

    @GetMapping("/{userId}/scraps")
    public ResponseEntity<Page<PostResponseDto>> getMyScraps(
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        if (userDetails == null) throw new AccessDeniedException("로그인이 필요합니다.");
        Page<PostResponseDto> scrapPage = postService.getScrappedPostsByUsername(userDetails.getUsername(), pageable);
        return ResponseEntity.ok(scrapPage);
    }
}